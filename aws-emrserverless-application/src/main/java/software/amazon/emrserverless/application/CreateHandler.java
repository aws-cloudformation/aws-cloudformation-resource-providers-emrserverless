package software.amazon.emrserverless.application;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.Application;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.CreateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.CreateApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.InternalServerException;
import software.amazon.awssdk.services.emrserverless.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    private static final List<ApplicationState> DESIRED_CREATE_END_STATES = Lists.newArrayList(ApplicationState.CREATED,
        ApplicationState.STARTED,
        ApplicationState.STOPPED);

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<EmrServerlessClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress -> createApplication(proxy, request, progress.getResourceModel(), proxyClient, callbackContext))
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createApplication(final AmazonWebServicesClientProxy proxy,
                                                                            final ResourceHandlerRequest<ResourceModel> request,
                                                                            final ResourceModel resourceModel,
                                                                            final ProxyClient<EmrServerlessClient> proxyClient,
                                                                            final CallbackContext callbackContext) {
        return proxy.initiate("AWS-EMRServerless-Application::Create", proxyClient, resourceModel,callbackContext)
                .translateToServiceRequest((model -> Translator.translateToCreateRequest(model, request)))
                .makeServiceCall((createApplicationRequest, proxyInvocationClient) -> {
                    CreateApplicationResponse createApplicationResponse = callCreateApplication(createApplicationRequest, proxyInvocationClient);
                    resourceModel.setApplicationId(createApplicationResponse.applicationId());
                    return createApplicationResponse;
                })
                .stabilize((awsRequest, awsResponse, proxyInvocation, model, context) -> isStabilizedForCreate(model, proxyInvocation, context))
                .handleError(this::handleError)
                .progress();
    }


     //Calls downstream service to create the application resource
    private CreateApplicationResponse callCreateApplication(final CreateApplicationRequest createApplicationRequest,
                                                           final ProxyClient<EmrServerlessClient> proxyClient) {
        try {
            CreateApplicationResponse response = proxyClient.injectCredentialsAndInvokeV2(createApplicationRequest, proxyClient.client()::createApplication);
            logger.log(String.format("[INFO] %s has successfully been created.", ResourceModel.TYPE_NAME));
            return response;
        } catch (AwsServiceException exception) {
            logger.log(String.format("Failed to create application: %s for request %s", exception.getMessage(), createApplicationRequest));
            throw  exception;
        }
    }

    // Determines if the created application resource has completed successfully
    private boolean isStabilizedForCreate(final ResourceModel model,
                                          final ProxyClient<EmrServerlessClient> proxyClient,
                                          final CallbackContext context) {

        GetApplicationResponse response = null;
        try {
            response = readActiveResource(Translator.translateToReadRequest(model), proxyClient);
            ApplicationState applicationState = Optional.ofNullable(response)
                .map(GetApplicationResponse::application)
                .map(Application::state)
                .orElse(ApplicationState.UNKNOWN_TO_SDK_VERSION);
            if (DESIRED_CREATE_END_STATES.contains(applicationState)) {
                return true;
            }
        } catch (ResourceNotFoundException e) {
            context.setStabilizationFailed(true);
            throw InternalServerException.builder()
                .message(String.format(HandlerErrorCode.NotStabilized.getMessage(), ResourceModel.TYPE_NAME, model.getApplicationId()))
                .build();
        }
        return false;
    }
}