package software.amazon.emrserverless.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.RetryableException;
import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.Application;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.ConflictException;
import software.amazon.awssdk.services.emrserverless.model.CreateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.DeleteApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.EmrServerlessException;
import software.amazon.awssdk.services.emrserverless.model.EmrServerlessResponseMetadata;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.InternalServerException;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsRequest;
import software.amazon.awssdk.services.emrserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.emrserverless.model.TagResourceRequest;
import software.amazon.awssdk.services.emrserverless.model.UntagResourceRequest;
import software.amazon.awssdk.services.emrserverless.model.UpdateApplicationRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    static final Map<Class<? extends AwsRequest>, String> REQUEST_TO_OPERATION =
            ImmutableMap.<Class<? extends AwsRequest>, String>builder()
                    .put(GetApplicationRequest.class, "GetApplication")
                    .put(CreateApplicationRequest.class, "CreateApplication")
                    .put(UpdateApplicationRequest.class, "UpdateApplication")
                    .put(DeleteApplicationRequest.class, "DeleteApplication")
                    .put(ListApplicationsRequest.class, "ListApplications")
                    .put(TagResourceRequest.class, "TagResource")
                    .put(UntagResourceRequest.class, "UntagResource")
                    .build();

    static final Set<Class<? extends Exception>> RETRYABLE_EXCEPTIONS =
            ImmutableSet.<Class<? extends Exception>>builder()
                    .add(ConflictException.class)
                    .add(InternalServerException.class)
                    .build();

    static final List<ApplicationState> INACTIVE_APPLICATION_STATES = Lists.newArrayList(ApplicationState.TERMINATED,
        ApplicationState.UNKNOWN_TO_SDK_VERSION);

    static final List<ApplicationState> ACTIVE_APPLICATION_STATES = Lists.newArrayList(ApplicationState.CREATED,
        ApplicationState.CREATING,
        ApplicationState.STARTED,
        ApplicationState.STARTING,
        ApplicationState.STOPPED,
        ApplicationState.STOPPING);

    Logger logger;

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    /***
     * Call GetApplication API and returns resource object if application is active, else throw ResourceNotFoundException
     * @param request
     * @param proxyClient
     * @return GetApplicationResponse
     * @throws ResourceNotFoundException
     */
    protected GetApplicationResponse readActiveResource(final GetApplicationRequest request,
                                                  final ProxyClient<EmrServerlessClient> proxyClient) throws ResourceNotFoundException {
        GetApplicationResponse response = readResource(request, proxyClient);
        final ApplicationState applicationState = Optional.ofNullable(response)
            .map(GetApplicationResponse::application)
            .map(Application::state)
            .orElse(ApplicationState.UNKNOWN_TO_SDK_VERSION);

        //If application state is not same as any of the states in applicationStateFilter, then throw ResourceNotFoundException
        if (INACTIVE_APPLICATION_STATES.contains(applicationState)) {
            throw ResourceNotFoundException.builder()
                .message(String.format("[ERROR] Application id: %s isn't active, state: %s", request.applicationId(), applicationState))
                .build();
        }
        return response;
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleError(
            final AwsRequest request,
            final Exception e,
            final ProxyClient<EmrServerlessClient> emrServerlessClientProxyClient,
            final ResourceModel resourceModel,
            final CallbackContext callbackContext) {
        String operation = Optional.ofNullable(request)
                .map(AwsRequest::getClass)
                .map(REQUEST_TO_OPERATION::get)
                .orElse(null);
        logger.log(String.format("[ERROR] handleError for %s, error: %s", operation, e));
        if (isRetryableException(e) && callbackContext.getRetryAttempts() > 0 && !callbackContext.isStabilizationFailed()) {
            // this will allow failed operation to retry
            callbackContext.retryAttempts = callbackContext.getRetryAttempts() - 1;
            throw RetryableException.create(e.getMessage(), e);
        }

        BaseHandlerException ex = (e instanceof EmrServerlessException)
                ? Translator.translate((EmrServerlessException) e, operation, resourceModel.getApplicationId(), callbackContext)
                : new CfnGeneralServiceException(operation, e);
        return ProgressEvent.failed(resourceModel, callbackContext, ex.getErrorCode(), ex.getMessage());
    }

    protected boolean isRetryableException(final Exception e) {
        return Optional.ofNullable(e)
                .map(Exception::getClass)
                .map(RETRYABLE_EXCEPTIONS::contains)
                .orElse(false);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<EmrServerlessClient> proxyClient,
            final Logger logger);

    /***
     * Call GetApplication API and returns resource object if application.
     * @param request
     * @param proxyClient
     * @return GetApplicationResponse
     * @throws ResourceNotFoundException
     */
    private GetApplicationResponse readResource(final GetApplicationRequest request,
                                                final ProxyClient<EmrServerlessClient> proxyClient) throws ResourceNotFoundException {
        GetApplicationResponse response;
        try {
            logger.log(String.format("[INFO] Invoking getApplication with request: %s", request));
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getApplication);

            final String requestId = Optional.ofNullable(response)
                .map(GetApplicationResponse::responseMetadata)
                .map(EmrServerlessResponseMetadata::requestId)
                .orElse(null);
            logger.log(String.format("[INFO] Received getApplication response for requestId: %s, response: %s", requestId, response));
        } catch (final AwsServiceException e) {
            logger.log(String.format("[ERROR] Exception thrown while calling getApplication with request: %s", request));
            throw e;
        }
        logger.log(String.format("[INFO] %s has successfully been read.", ResourceModel.TYPE_NAME));
        return response;
    }
}
