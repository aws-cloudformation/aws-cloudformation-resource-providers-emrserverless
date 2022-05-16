package software.amazon.emrserverless.application;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EmrServerlessClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress -> deleteApplicationPreCheck(proxy, progress.getResourceModel(), proxyClient, progress.getCallbackContext()))
            .then(progress -> deleteApplication(proxy, progress.getResourceModel(), proxyClient, progress.getCallbackContext()));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteApplicationPreCheck(final AmazonWebServicesClientProxy proxy,
                                                                                    final ResourceModel resourceModel,
                                                                                    final ProxyClient<EmrServerlessClient> proxyClient,
                                                                                    final CallbackContext callbackContext) {
        return proxy.initiate("AWS-EMRServerless-Application::Delete::PreDeletionCheck", proxyClient, resourceModel,
            callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall(this::readActiveResource)
            .handleError(this::handleError)
            .done((awsRequest, awsResponse, client, model, context) -> ProgressEvent.progress(model, context));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteApplication(final AmazonWebServicesClientProxy proxy,
                                                                            final ResourceModel resourceModel,
                                                                            final ProxyClient<EmrServerlessClient> client,
                                                                            final CallbackContext callbackContext) {

        return proxy.initiate("AWS-EMRServerless-Application::Delete", client, resourceModel, callbackContext)
            .translateToServiceRequest(Translator::translateToDeleteRequest)
            .makeServiceCall((awsRequest, proxyClient) -> proxyClient.injectCredentialsAndInvokeV2(awsRequest,
                proxyClient.client()::deleteApplication))
            .stabilize((awsRequest, awsResponse, proxyClient, model, context) -> isStabilized(proxyClient, model))
            .handleError(this::handleError)
            .done((awsRequest) -> ProgressEvent.<ResourceModel, CallbackContext>builder().status(OperationStatus.SUCCESS).build());
    }

    /**
     * Stabilization for Delete operation.
     *
     * @param proxyClient   object to call API
     * @param resourceModel ResourceModel object
     * @return boolean true if stabilized else false
     */
    public boolean isStabilized(final ProxyClient<EmrServerlessClient> proxyClient,
                                final ResourceModel resourceModel) {

        try {
            readActiveResource(Translator.translateToReadRequest(resourceModel), proxyClient);
            return false; //Return false if response is for active resource is returned
        } catch (ResourceNotFoundException e) {
            //Delete is stabilized when ResourceNotFound, i.e. resource is successfully deleted
            return true;
        }
    }
}
