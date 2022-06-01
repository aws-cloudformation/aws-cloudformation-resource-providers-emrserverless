package software.amazon.emrserverless.application;

import org.apache.commons.lang3.StringUtils;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class ReadHandler extends BaseHandlerStd {
    private static final String CALL_GRAPH = "AWS-EMRServerless-Application::Read";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<EmrServerlessClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        logger.log(String.format("[INFO] Read handler request: %s", request));

        final ResourceModel model = request.getDesiredResourceState();
        if (StringUtils.isEmpty(model.getApplicationId())) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.NotFound, "ApplicationId was not provided");
        }
        return proxy.initiate(CALL_GRAPH, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall(this::readActiveResource)
                .handleError(this::handleError)
                .done(readResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(readResponse)));
    }
}
