package software.amazon.emrserverless.application;

import java.util.Optional;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.EmrServerlessResponseMetadata;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsRequest;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<EmrServerlessClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        logger.log(String.format("[INFO] List handler request: %s", request));

        return proxy.initiate("AWS-EMRServerless-Application::List", proxyClient, request.getDesiredResourceState(), callbackContext)
            .translateToServiceRequest(model -> Translator.translateToListRequest(request.getNextToken(), ACTIVE_APPLICATION_STATES))
            .makeServiceCall(this::callListApplications)
            .handleError(this::handleError)
            .done((awsResponse) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(Translator.translateFromListResponse(awsResponse))
                .nextToken(awsResponse.nextToken())
                .status(OperationStatus.SUCCESS)
                .build());
    }

    private ListApplicationsResponse callListApplications(final ListApplicationsRequest request,
                                                          final ProxyClient<EmrServerlessClient> proxyClient) {

        ListApplicationsResponse response;
        try {
            logger.log(String.format("[INFO] Invoking ListApplications with request: %s", request));
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::listApplications);

            final String requestId = Optional.ofNullable(response)
                .map(ListApplicationsResponse::responseMetadata)
                .map(EmrServerlessResponseMetadata::requestId)
                .orElse(null);
            logger.log(String.format("[INFO] Received ListApplications requestId: %s response: %s", requestId, response));
        } catch (final Exception e) {
            logger.log(String.format("[ERROR] Exception thrown while invoking ListApplications, error: %s", e.getMessage(), e));
            throw e;
        }
        return response;
    }
}
