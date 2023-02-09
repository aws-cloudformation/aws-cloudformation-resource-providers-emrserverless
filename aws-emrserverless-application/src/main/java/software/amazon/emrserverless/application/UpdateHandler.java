package software.amazon.emrserverless.application;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.Application;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.TagResourceResponse;
import software.amazon.awssdk.services.emrserverless.model.UntagResourceResponse;
import software.amazon.awssdk.services.emrserverless.model.UpdateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.UpdateApplicationResponse;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Updates the resource.
 */
public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<EmrServerlessClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        logger.log(String.format("[INFO] Update handler request: %s", request));
        final ResourceModel model = request.getDesiredResourceState();
        //validation
        if (StringUtils.isEmpty(model.getApplicationId())) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.NotFound, "ApplicationId must be provided");
        }
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateApplicationPreCheck(proxy, progress.getResourceModel(), proxyClient, callbackContext))
                .then(progress -> updateApplication(proxy, request, progress.getResourceModel(), proxyClient, callbackContext))
                .then(progress -> retrieveApplicationTags(proxy, request, progress.getResourceModel(), proxyClient, callbackContext))
                .then(progress -> removeTagsIfNeeded(proxy, progress.getResourceModel(), proxyClient, callbackContext))
                .then(progress -> addTagsIfNeeded(proxy, progress.getResourceModel(), proxyClient, callbackContext))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }


    private ProgressEvent<ResourceModel, CallbackContext> updateApplicationPreCheck(final AmazonWebServicesClientProxy proxy,
                                                                                    final ResourceModel resourceModel,
                                                                                    final ProxyClient<EmrServerlessClient> proxyClient,
                                                                                    final CallbackContext callbackContext) {
        return proxy.initiate("AWS-EMRServerless-Application::Update::PreUpdateCheck", proxyClient, resourceModel,
                        callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall(this::readActiveResource)
                .handleError(this::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateApplication(final AmazonWebServicesClientProxy proxy,
                                                                            final ResourceHandlerRequest<ResourceModel> request,
                                                                            final ResourceModel resourceModel,
                                                                            final ProxyClient<EmrServerlessClient> proxyClient,
                                                                            final CallbackContext callbackContext) {
        return proxy.initiate("AWS-EMRServerless-Application::Update", proxyClient, resourceModel,callbackContext)
                .translateToServiceRequest((model -> Translator.translateToUpdateRequest(model, request)))
                .makeServiceCall(this::callUpdateApplication)
                .handleError(this::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> retrieveApplicationTags(final AmazonWebServicesClientProxy proxy,
                                                                                  final ResourceHandlerRequest<ResourceModel> request,
                                                                                  final ResourceModel resourceModel,
                                                                                  final ProxyClient<EmrServerlessClient> proxyClient,
                                                                                  final CallbackContext callbackContext) {
        return proxy.initiate("AWS-EMRServerless-Application::RetrieveTags", proxyClient, resourceModel, callbackContext)
                .translateToServiceRequest((model -> Translator.translateToReadRequest(resourceModel)))
                .makeServiceCall((getApplicationRequest, proxyInvocationClient) -> {
                    GetApplicationResponse getApplicationResponse = readActiveResource(getApplicationRequest, proxyClient);
                    updateTagsInContext(request, getApplicationResponse, callbackContext);
                    return getApplicationResponse;
                })
                .handleError(this::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> removeTagsIfNeeded(final AmazonWebServicesClientProxy proxy,
                                                                             final ResourceModel resourceModel,
                                                                             final ProxyClient<EmrServerlessClient> proxyClient,
                                                                             final CallbackContext callbackContext) {
        if (!CollectionUtils.isNullOrEmpty(callbackContext.tagsToRemove)) {
            return untagResource(proxy, proxyClient, resourceModel, callbackContext, callbackContext.tagsToRemove, logger);
        } else {
            return ProgressEvent.progress(resourceModel, callbackContext);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> addTagsIfNeeded(final AmazonWebServicesClientProxy proxy,
                                                                          final ResourceModel resourceModel,
                                                                          final ProxyClient<EmrServerlessClient> proxyClient,
                                                                          final CallbackContext callbackContext) {
        if (!CollectionUtils.isNullOrEmpty(callbackContext.tagsToAdd)) {
            return tagResource(proxy, proxyClient, resourceModel, callbackContext, callbackContext.tagsToAdd, logger);
        } else {
            return ProgressEvent.progress(resourceModel, callbackContext);
        }
    }

    private UpdateApplicationResponse callUpdateApplication(final UpdateApplicationRequest updateApplicationRequest,
                                                            final ProxyClient<EmrServerlessClient> proxyClient) {
        try {
            UpdateApplicationResponse response = proxyClient.injectCredentialsAndInvokeV2(updateApplicationRequest, proxyClient.client()::updateApplication);
            logger.log(String.format("[INFO] %s has successfully been updated.", ResourceModel.TYPE_NAME));
            return response;
        } catch (AwsServiceException e) {
            logger.log(String.format("Failed to update application: %s for request %s", e.getMessage(), updateApplicationRequest));
            throw  e;
        }
    }

    private void updateTagsInContext(final ResourceHandlerRequest<ResourceModel> request,
                                     final GetApplicationResponse getApplicationResponse,
                                     final CallbackContext callbackContext) {
        final Map<String, String> existingTags = Optional.ofNullable(getApplicationResponse)
                .map(GetApplicationResponse::application)
                .map(Application::tags)
                .orElse(Collections.emptyMap());
        final Map<String, String> desiredTags = request.getDesiredResourceTags();
        final Set<String> tagsToRemove = TagHelper.generateTagsToRemove(existingTags, desiredTags);
        final Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(existingTags, desiredTags);
        callbackContext.setTagsToRemove(tagsToRemove);
        callbackContext.setTagsToAdd(tagsToAdd);
        String applicationArn = Optional.ofNullable(getApplicationResponse)
            .map(GetApplicationResponse::application)
            .map(Application::arn)
            .orElse(null);
        callbackContext.setApplicationArn(applicationArn);
    }

    private ProgressEvent<ResourceModel, CallbackContext> tagResource(final AmazonWebServicesClientProxy proxy,
                                                                     final ProxyClient<EmrServerlessClient> proxyClient,
                                                                     final ResourceModel resourceModel,
                                                                     final CallbackContext callbackContext,
                                                                     final Map<String, String> tagsToAdd,
                                                                     final Logger logger) {
        logger.log(String.format("[UPDATE][IN PROGRESS] Going to add tags for resource: %s ", ResourceModel.TYPE_NAME));
        return proxy.initiate("AWS-EMRServerless-Application::TagOps", proxyClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.tagResourceRequest(callbackContext.getApplicationArn(), tagsToAdd))
                .makeServiceCall((request, client) -> {
                    try {
                        TagResourceResponse tagResourceResponse = proxy.injectCredentialsAndInvokeV2(request, proxyClient.client()::tagResource);
                        logger.log(String.format("[INFO] %s:%s has successfully been tagged.", ResourceModel.TYPE_NAME, resourceModel.getApplicationId()));
                        return tagResourceResponse;
                    } catch (AwsServiceException e) {
                        logger.log(String.format("Failed to add tags. Error: %s for Application %s", e.getMessage(), resourceModel.getApplicationId()));
                        throw  e;
                    }
                })
                .handleError(this::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> untagResource(final AmazonWebServicesClientProxy proxy,
                                                                       final ProxyClient<EmrServerlessClient> proxyClient,
                                                                       final ResourceModel resourceModel,
                                                                       final CallbackContext callbackContext,
                                                                       final Set<String> tagsToRemove,
                                                                       final Logger logger) {
        logger.log(String.format("[UPDATE][IN PROGRESS] Going to remove tags for resource: %s ", ResourceModel.TYPE_NAME));
        return proxy.initiate("AWS-EMRServerless-Application::UnTagOps", proxyClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.untagResourceRequest(callbackContext.getApplicationArn(), tagsToRemove))
                .makeServiceCall((request, client) -> {
                    try {
                        UntagResourceResponse untagResourceResponse = proxy.injectCredentialsAndInvokeV2(request, proxyClient.client()::untagResource);
                        logger.log(String.format("[INFO] %s:%s has successfully been untagged.", ResourceModel.TYPE_NAME, resourceModel.getApplicationId()));
                        return untagResourceResponse;
                    } catch (AwsServiceException e) {
                        logger.log(String.format("Failed to remove tags. Error: %s for Application %s", e.getMessage(), resourceModel.getApplicationId()));
                        throw  e;
                    }
                })
                .handleError(this::handleError)
                .progress();
    }
}
