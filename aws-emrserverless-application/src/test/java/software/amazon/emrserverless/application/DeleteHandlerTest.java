package software.amazon.emrserverless.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableMap;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.DeleteApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.DeleteApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.InternalServerException;
import software.amazon.awssdk.services.emrserverless.model.ResourceNotFoundException;
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
public class DeleteHandlerTest extends AbstractTestBase {

    private static final String DELETE_OPERATION = "DeleteApplication";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EmrServerlessClient> proxyClient;

    @Mock
    EmrServerlessClient sdkClient;

    private DeleteHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(EmrServerlessClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new DeleteHandler();
    }

    @AfterEach
    public void tear_down() {
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        GetApplicationResponse applicationCreatedResponse = getApplicationResponse(
                getApplication(APPLICATION_ID, ApplicationState.CREATED));
        GetApplicationResponse applicationTerminatedResponse = getApplicationResponse(
                getApplication(APPLICATION_ID, ApplicationState.TERMINATED));

        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
            .thenReturn(applicationCreatedResponse).thenReturn(applicationTerminatedResponse);
        when(sdkClient.deleteApplication(any(DeleteApplicationRequest.class))).thenReturn(DeleteApplicationResponse.builder().build());

        final ResourceModel model = ResourceModel.builder().applicationId(APPLICATION_ID).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient, times(2)).getApplication(any(GetApplicationRequest.class));
        verify(sdkClient, atLeastOnce()).deleteApplication(any(DeleteApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_terminatedResource() {

        GetApplicationResponse applicationTerminatedResponse = getApplicationResponse(
                getApplication(APPLICATION_ID, ApplicationState.TERMINATED));

        when(sdkClient.getApplication(any(GetApplicationRequest.class))).thenReturn(applicationTerminatedResponse);

        final ResourceModel model = ResourceModel.builder().applicationId(APPLICATION_ID).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient, times(1)).getApplication(any(GetApplicationRequest.class));
        verify(sdkClient, never()).deleteApplication(any(DeleteApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void handleRequest_ResourceNotFoundException() {
        when(sdkClient.getApplication(any(GetApplicationRequest.class))).thenThrow(ResourceNotFoundException.class);

        final ResourceModel model = ResourceModel.builder().applicationId(APPLICATION_ID).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient, times(1)).getApplication(any(GetApplicationRequest.class));
        verify(sdkClient, never()).deleteApplication(any(DeleteApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void handleRequest_InternalServerExceptionInGetApplication() {
        when(sdkClient.getApplication(any(GetApplicationRequest.class))).thenThrow(InternalServerException.class);

        final ResourceModel model = ResourceModel.builder().applicationId(APPLICATION_ID).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient, times(6)).getApplication(any(GetApplicationRequest.class));
        verify(sdkClient, never()).deleteApplication(any(DeleteApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @ParameterizedTest
    @MethodSource("exceptionArgumentsProvider")
    public void handleRequest_InternalServerExceptionInDeleteApplication(Exception sdkException, BaseHandlerException cfnException) {
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
            .thenReturn(getApplicationResponse(getApplication(APPLICATION_ID, ApplicationState.CREATED)));
        when(sdkClient.deleteApplication(any(DeleteApplicationRequest.class))).thenThrow(sdkException);

        final ResourceModel model = ResourceModel.builder().applicationId(APPLICATION_ID).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        int times = BaseHandlerStd.RETRYABLE_EXCEPTIONS.contains(sdkException.getClass())
            ? new CallbackContext().getRetryAttempts() + 1
            : 1;
        verify(sdkClient, times(1)).getApplication(any(GetApplicationRequest.class));
        assertThat(response).isNotNull();
        verify(sdkClient, times(times)).deleteApplication(any(DeleteApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(cfnException.getErrorCode());
        assertThat(response.getMessage()).containsSubsequence(cfnException.getMessage());
    }

    /**
     * Returns service exceptions to CFN exceptions map for all the valid exceptions that can be thrown by DeleteApplication API.
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
                DELETE_OPERATION, INTERNAL_SERVER_EXCEPTION))
            .build();
    }

}
