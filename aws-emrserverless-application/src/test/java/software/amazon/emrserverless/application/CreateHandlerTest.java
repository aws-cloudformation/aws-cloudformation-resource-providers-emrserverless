package software.amazon.emrserverless.application;

import java.time.Duration;
import java.util.Map;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.CreateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.CreateApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.InternalServerException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;

@TestInstance(PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    private static final String CREATE_OPERATION = "CreateApplication";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EmrServerlessClient> proxyClient;

    @Mock
    EmrServerlessClient sdkClient;

    private CreateHandler createHandler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(EmrServerlessClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        createHandler = new CreateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        CreateApplicationResponse createApplicationResponse = createApplicationResponse();
        GetApplicationResponse applicationCreatingResponse = getApplicationResponse(
            getApplication(APPLICATION_ID, ApplicationState.CREATING));
        GetApplicationResponse applicationCreatedResponse = getApplicationResponse(
            getApplication(APPLICATION_ID, ApplicationState.CREATED));

        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
            .thenReturn(applicationCreatingResponse).thenReturn(applicationCreatedResponse);
        when(sdkClient.createApplication(any(CreateApplicationRequest.class)))
            .thenReturn(createApplicationResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(getResourceModel(APPLICATION_ID))
            .desiredResourceTags(APPLICATION_TAGS)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).createApplication(any(CreateApplicationRequest.class));
        verify(sdkClient, times(3)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(getResourceModel(APPLICATION_ID));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessMovedToSTARTEDState() {
        CreateApplicationResponse createApplicationResponse = createApplicationResponse();
        GetApplicationResponse applicationCreatingResponse = getApplicationResponse(
            getApplication(APPLICATION_ID, ApplicationState.CREATING));
        GetApplicationResponse applicationCreatedResponse = getApplicationResponse(
            getApplication(APPLICATION_ID, ApplicationState.CREATED));

        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
            .thenReturn(applicationCreatingResponse).thenReturn(applicationCreatedResponse);
        when(sdkClient.createApplication(any(CreateApplicationRequest.class)))
            .thenReturn(createApplicationResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(ResourceModel.builder().build())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).createApplication(any(CreateApplicationRequest.class));
        verify(sdkClient, times(3)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(getResourceModel(APPLICATION_ID));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @ParameterizedTest
    @MethodSource("exceptionArgumentsProvider")
    public void handleRequest_exceptionInCreateApplication(Exception sdkException, BaseHandlerException cfnException) {
        when(sdkClient.createApplication(any(CreateApplicationRequest.class))).thenThrow(sdkException);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(ResourceModel.builder().build())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        int times = BaseHandlerStd.RETRYABLE_EXCEPTIONS.contains(sdkException.getClass())
            ? new CallbackContext().getRetryAttempts() + 1
            : 1;
        verify(sdkClient, times(times)).createApplication(any(CreateApplicationRequest.class));
        assertThat(response).isNotNull();
        verify(sdkClient, never()).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(cfnException.getErrorCode());
    }

    @Test
    public void handleRequest_UnableToVerifyStabilization() {
        CreateApplicationResponse createApplicationResponse = createApplicationResponse();

        when(sdkClient.createApplication(any(CreateApplicationRequest.class)))
            .thenReturn(createApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
            .thenThrow(InternalServerException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(ResourceModel.builder().build())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).createApplication(any(CreateApplicationRequest.class));
        verify(sdkClient, times(6)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @Test
    public void handleRequest_NotStabilized() {
        CreateApplicationResponse createApplicationResponse = createApplicationResponse();
        GetApplicationResponse applicationCreatingResponse = getApplicationResponse(
            getApplication(APPLICATION_ID, ApplicationState.CREATING));
        GetApplicationResponse applicationTerminatedResponse = getApplicationResponse(
            getApplication(APPLICATION_ID, ApplicationState.TERMINATED));

        when(sdkClient.createApplication(any(CreateApplicationRequest.class)))
            .thenReturn(createApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
            .thenReturn(applicationCreatingResponse).thenReturn(applicationTerminatedResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(ResourceModel.builder().build())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).createApplication(any(CreateApplicationRequest.class));
        verify(sdkClient, times(2)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }

    private CreateApplicationResponse createApplicationResponse() {
        return CreateApplicationResponse.builder()
            .applicationId(APPLICATION_ID)
            .arn(APPLICATION_ARN)
            .name(APPLICATION_NAME)
            .build();
    }

    /**
     * Returns service exceptions to CFN exceptions map for all the valid exceptions that can be thrown by CreateApplication API.
     *
     * @return Map
     */
    @Override
    protected Map<Exception, BaseHandlerException> getCFNExceptionMapping() {
        return ImmutableMap.<Exception, BaseHandlerException>builder()
            .put(VALIDATION_EXCEPTION, new CfnInvalidRequestException(
                VALIDATION_EXCEPTION.getMessage(), VALIDATION_EXCEPTION))
            .put(INTERNAL_SERVER_EXCEPTION, new CfnServiceInternalErrorException(
                CREATE_OPERATION, INTERNAL_SERVER_EXCEPTION))
            .put(CONFLICT_EXCEPTION, new CfnResourceConflictException(
                ResourceModel.TYPE_NAME, APPLICATION_ID, CONFLICT_EXCEPTION.getMessage(),
                CONFLICT_EXCEPTION))
            .build();
    }

}
