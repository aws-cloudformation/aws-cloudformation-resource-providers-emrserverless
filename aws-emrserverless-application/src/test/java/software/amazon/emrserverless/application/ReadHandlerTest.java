package software.amazon.emrserverless.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableMap;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@TestInstance(PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    private static final String READ_OPERATION = "GetApplication";

    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<EmrServerlessClient> proxyClient;
    private EmrServerlessClient sdkClient;
    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(EmrServerlessClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new ReadHandler();
    }

    @Test
    public void handleRequest_success() {
        //Setup
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(ResourceModel.builder()
                    .applicationId(APPLICATION_ID)
                    .build())
            .build();
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(getApplicationResponse(getApplication(APPLICATION_ID, ApplicationState.CREATED)));

        //Invoke
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        //Verify & Assert
        verify(sdkClient).getApplication(any(GetApplicationRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(getResourceModel(APPLICATION_ID));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void handleRequest_emptyResourceIdInput(String input) {
        //Setup
        final ResourceModel inputModel = ResourceModel.builder()
                .applicationId(input)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(inputModel)
                .build();

        //Invoke
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        //Verify & Assert
        verify(sdkClient, never()).getApplication(any(GetApplicationRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
        assertThat(response.getResourceModel()).isEqualTo(inputModel);
        assertThat(response.getResourceModels()).isNull();
    }

    @ParameterizedTest
    @MethodSource("exceptionArgumentsProvider")
    public void handleRequest_exception(Exception sdkException, BaseHandlerException cfnException) {
        //Setup
        final ResourceModel inputModel = ResourceModel.builder()
                .applicationId(APPLICATION_ID)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(inputModel)
                .build();
        when(sdkClient.getApplication(any(GetApplicationRequest.class))).thenThrow(sdkException);

        //Invoke
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        //Verify & Assert
        int times = BaseHandlerStd.RETRYABLE_EXCEPTIONS.contains(sdkException.getClass())
                ? new CallbackContext().getRetryAttempts() + 1
                : 1;
        verify(sdkClient, times(times)).getApplication(any(GetApplicationRequest.class));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(cfnException.getErrorCode());
        assertThat(response.getMessage()).isEqualTo(cfnException.getMessage());
        assertThat(response.getResourceModel()).isEqualTo(inputModel);
        assertThat(response.getResourceModels()).isNull();
    }

    /**
     * Returns service exceptions to CFN exceptions map for all the valid exceptions that can be thrown by GetApplication API.
     *
     * @return Map
     */
    @Override
    protected Map<Exception, BaseHandlerException> getCFNExceptionMapping() {
        return ImmutableMap.<Exception, BaseHandlerException>builder()
            .put(VALIDATION_EXCEPTION, new CfnInvalidRequestException(
                VALIDATION_EXCEPTION.getMessage(), VALIDATION_EXCEPTION))
            .put(NOT_FOUND_EXCEPTION, new CfnNotFoundException(
                ResourceModel.TYPE_NAME, APPLICATION_ID))
            .put(INTERNAL_SERVER_EXCEPTION, new CfnServiceInternalErrorException(
                READ_OPERATION, INTERNAL_SERVER_EXCEPTION))
            .build();
    }
}