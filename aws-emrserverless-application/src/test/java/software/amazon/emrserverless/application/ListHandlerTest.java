package software.amazon.emrserverless.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableMap;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsRequest;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@TestInstance(PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    private static final String LIST_OPERATION = "ListApplications";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EmrServerlessClient> proxyClient;

    @Mock
    EmrServerlessClient sdkClient;

    private ListHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(EmrServerlessClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        this.handler = new ListHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        ListApplicationsResponse listApplicationsResponse = getListApplicationsResponse();
        when(sdkClient.listApplications(any(ListApplicationsRequest.class))).thenReturn(listApplicationsResponse);

        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .nextToken(NEXT_TOKEN_1)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).listApplications(any(ListApplicationsRequest.class));
        assertThat(response.getNextToken()).isEqualTo(NEXT_TOKEN_2);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void handleRequest_nullOrEmptyNextToken_SimpleSuccess(String token) {

        ListApplicationsResponse listApplicationsResponse = getListApplicationsResponse();
        when(sdkClient.listApplications(any(ListApplicationsRequest.class))).thenReturn(listApplicationsResponse);

        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .nextToken(token)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).listApplications(any(ListApplicationsRequest.class));
        assertThat(response.getNextToken()).isEqualTo(NEXT_TOKEN_2);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @ParameterizedTest
    @MethodSource("exceptionArgumentsProvider")
    public void handleRequest_exception(Exception sdkException, BaseHandlerException cfnException) {

        when(sdkClient.listApplications(any(ListApplicationsRequest.class))).thenThrow(sdkException);

        final ResourceModel model = ResourceModel.builder().build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .nextToken(NEXT_TOKEN_1)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        int times = BaseHandlerStd.RETRYABLE_EXCEPTIONS.contains(sdkException.getClass())
            ? new CallbackContext().getRetryAttempts() + 1
            : 1;
        verify(sdkClient, times(times)).listApplications(any(ListApplicationsRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNotNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNotNull();
        assertThat(response.getErrorCode()).isEqualTo(cfnException.getErrorCode());
        assertThat(response.getMessage()).containsSubsequence(cfnException.getMessage());
    }

    /**
     * Returns service exceptions to CFN exceptions map for all the valid exceptions that can be thrown by ListApplications API.
     * @return Map
     */
    @Override
    protected Map<Exception, BaseHandlerException> getCFNExceptionMapping() {
        return ImmutableMap.<Exception, BaseHandlerException>builder()
            .put(VALIDATION_EXCEPTION, new CfnInvalidRequestException(
                VALIDATION_EXCEPTION.getMessage(), VALIDATION_EXCEPTION))
            .put(INTERNAL_SERVER_EXCEPTION, new CfnServiceInternalErrorException(
                LIST_OPERATION, INTERNAL_SERVER_EXCEPTION))
            .build();
    }
}
