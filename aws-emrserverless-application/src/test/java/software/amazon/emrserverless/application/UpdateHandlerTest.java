package software.amazon.emrserverless.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.Application;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.AutoStartConfig;
import software.amazon.awssdk.services.emrserverless.model.AutoStopConfig;
import software.amazon.awssdk.services.emrserverless.model.Configuration;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.InternalServerException;
import software.amazon.awssdk.services.emrserverless.model.ManagedPersistenceMonitoringConfiguration;
import software.amazon.awssdk.services.emrserverless.model.S3MonitoringConfiguration;
import software.amazon.awssdk.services.emrserverless.model.TagResourceRequest;
import software.amazon.awssdk.services.emrserverless.model.TagResourceResponse;
import software.amazon.awssdk.services.emrserverless.model.UntagResourceRequest;
import software.amazon.awssdk.services.emrserverless.model.UntagResourceResponse;
import software.amazon.awssdk.services.emrserverless.model.UpdateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.UpdateApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.WorkerResourceConfig;
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
public class UpdateHandlerTest extends AbstractTestBase {

    private static final Map<String, String> INITIAL_APPLICATION_TAGS = ImmutableMap.of(
        "tag-key-1", "tag-value-1",
        "tag-key-2", "tag-value-2",
        "tag-key-3", "tag-value-3"
    );
    private static final Map<String, String> DESIRED_APPLICATION_TAGS = ImmutableMap.of(
        "tag-key-4", "tag-value-4",
        "tag-key-5", "tag-value-5",
        "tag-key-3", "tag-value-3"
    );
    private static final String UPDATE_OPERATION = "UpdateApplication";
    private static final String UPDATED_RELEASE_LABEL = "spark-6.10.0-preview";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<EmrServerlessClient> proxyClient;

    @Mock
    EmrServerlessClient sdkClient;

    private UpdateHandler updateHandler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(EmrServerlessClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        updateHandler = new UpdateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        UpdateApplicationResponse updateApplicationResponse = updateApplicationResponse();
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, INITIAL_APPLICATION_TAGS));
        GetApplicationResponse postUpdateApplicationResponse = getApplicationResponse(getUpdatedApplication(ApplicationState.CREATED, DESIRED_APPLICATION_TAGS));

        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(updateApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse)
                .thenReturn(initialApplicationResponse)
                .thenReturn(postUpdateApplicationResponse);
        when(sdkClient.tagResource(any(TagResourceRequest.class)))
                .thenReturn(TagResourceResponse.builder().build());
        when(sdkClient.untagResource(any(UntagResourceRequest.class)))
                .thenReturn(UntagResourceResponse.builder().build());

        final ResourceModel model = Translator.translateFromReadResponse(postUpdateApplicationResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceTags(DESIRED_APPLICATION_TAGS)
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient).tagResource(any(TagResourceRequest.class));
        verify(sdkClient).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient, times(3)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    private Stream<Arguments> provideApplicationWithApplicationConfiguration() {
        return Stream.of(
                Arguments.of(getDefaultApplicationBuilder()
                                .state(ApplicationState.CREATED)
                        .monitoringConfiguration(software.amazon.awssdk.services.emrserverless.model.MonitoringConfiguration.builder()
                                .s3MonitoringConfiguration(software.amazon.awssdk.services.emrserverless.model.S3MonitoringConfiguration
                                        .builder().encryptionKeyArn(null)
                                        .logUri(null)
                                        .build())
                                .managedPersistenceMonitoringConfiguration(software.amazon.awssdk.services.emrserverless.model
                                        .ManagedPersistenceMonitoringConfiguration.builder()
                                        .enabled(null)
                                        .encryptionKeyArn(null).build())
                                .build())
                        .runtimeConfiguration(Sets.newHashSet(software.amazon.awssdk.services.emrserverless.model.Configuration.builder()
                                .classification(null)
                                .properties(null)
                                .configurations((Collection<Configuration>) null)
                                .build()))
                        .build()),
                Arguments.of(getDefaultApplicationBuilder()
                        .state(ApplicationState.CREATED)
                        .monitoringConfiguration(software.amazon.awssdk.services.emrserverless.model.MonitoringConfiguration.builder()
                                .s3MonitoringConfiguration((S3MonitoringConfiguration) null)
                                .managedPersistenceMonitoringConfiguration((ManagedPersistenceMonitoringConfiguration) null)
                                .build())
                        .runtimeConfiguration(Sets.newHashSet())
                        .build())
        );
    }

    @MethodSource("provideApplicationWithApplicationConfiguration")
    @ParameterizedTest
    public void handleRequest_SuccessWithApplicationConfiguration(Application application) {
        UpdateApplicationResponse updateApplicationResponse = updateApplicationResponse();
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, INITIAL_APPLICATION_TAGS));
        GetApplicationResponse postUpdateApplicationResponse = getApplicationResponse(application);

        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(updateApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse)
                .thenReturn(initialApplicationResponse)
                .thenReturn(postUpdateApplicationResponse);
        when(sdkClient.tagResource(any(TagResourceRequest.class)))
                .thenReturn(TagResourceResponse.builder().build());
        when(sdkClient.untagResource(any(UntagResourceRequest.class)))
                .thenReturn(UntagResourceResponse.builder().build());

        final ResourceModel model = Translator.translateFromReadResponse(postUpdateApplicationResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceTags(DESIRED_APPLICATION_TAGS)
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient).tagResource(any(TagResourceRequest.class));
        verify(sdkClient).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient, times(3)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessWithUpdatedReleaseLabel() {
        UpdateApplicationResponse updateApplicationResponse = updateApplicationResponse();
        GetApplicationResponse initialApplicationResponse =
                getApplicationResponse(getApplication(ApplicationState.CREATED, Collections.emptyMap()));
        GetApplicationResponse postUpdateApplicationResponse =
                getApplicationResponse(getApplicationWithUpdatedReleaseLabel(ApplicationState.CREATED,
                        Collections.emptyMap()));

        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(updateApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse)
                .thenReturn(initialApplicationResponse)
                .thenReturn(postUpdateApplicationResponse);
        final ResourceModel model = ResourceModel.builder()
                .arn(APPLICATION_ARN)
                .applicationId(APPLICATION_ID)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(Collections.emptyMap())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient, never()).tagResource(any(TagResourceRequest.class));
        verify(sdkClient, never()).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient, times(3)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(getResourceModel(APPLICATION_ID, Collections.emptyMap(), UPDATED_RELEASE_LABEL));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessWithNoTags() {
        UpdateApplicationResponse updateApplicationResponse = updateApplicationResponse();
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, Collections.emptyMap()));
        GetApplicationResponse postUpdateApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, Collections.emptyMap()));

        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(updateApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse)
                .thenReturn(initialApplicationResponse)
                .thenReturn(postUpdateApplicationResponse);
        final ResourceModel model = ResourceModel.builder()
                .arn(APPLICATION_ARN)
                .applicationId(APPLICATION_ID)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(Collections.emptyMap())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient, never()).tagResource(any(TagResourceRequest.class));
        verify(sdkClient, never()).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient, times(3)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(getResourceModel(APPLICATION_ID, Collections.emptyMap()));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }


    @Test
    public void handleRequest_SuccessWithTagging() {
        UpdateApplicationResponse updateApplicationResponse = updateApplicationResponse();
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, Collections.emptyMap()));
        GetApplicationResponse postUpdateApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, DESIRED_APPLICATION_TAGS));

        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(updateApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse)
                .thenReturn(initialApplicationResponse)
                .thenReturn(postUpdateApplicationResponse);
        when(sdkClient.tagResource(any(TagResourceRequest.class)))
                .thenReturn(TagResourceResponse.builder().build());

        final ResourceModel model = ResourceModel.builder()
                .arn(APPLICATION_ARN)
                .applicationId(APPLICATION_ID)
                .tags(TagHelper.convertToSet(DESIRED_APPLICATION_TAGS))
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(DESIRED_APPLICATION_TAGS)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient).tagResource(any(TagResourceRequest.class));
        verify(sdkClient, never()).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient, times(3)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(getResourceModel(APPLICATION_ID, DESIRED_APPLICATION_TAGS));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessWithUnTagging() {
        UpdateApplicationResponse updateApplicationResponse = updateApplicationResponse();
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, INITIAL_APPLICATION_TAGS));
        GetApplicationResponse postUpdateApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, Collections.emptyMap()));

        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(updateApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse)
                .thenReturn(initialApplicationResponse)
                .thenReturn(postUpdateApplicationResponse);
        when(sdkClient.untagResource(any(UntagResourceRequest.class)))
                .thenReturn(UntagResourceResponse.builder().build());

        final ResourceModel model = ResourceModel.builder()
                .arn(APPLICATION_ARN)
                .applicationId(APPLICATION_ID)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(Collections.emptyMap())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient, never()).tagResource(any(TagResourceRequest.class));
        verify(sdkClient).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient, times(3)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(getResourceModel(APPLICATION_ID, Collections.emptyMap()));
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @ParameterizedTest
    @MethodSource("exceptionArgumentsProvider")
    public void handleRequest_exceptionInUpdateApplication(Exception sdkException, BaseHandlerException cfnException) {
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, INITIAL_APPLICATION_TAGS));
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse);
        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class))).thenThrow(sdkException);

        final ResourceModel model = ResourceModel.builder()
            .arn(APPLICATION_ARN)
            .applicationId(APPLICATION_ID)
            .tags(TagHelper.convertToSet(DESIRED_APPLICATION_TAGS))
            .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        int times = BaseHandlerStd.RETRYABLE_EXCEPTIONS.contains(sdkException.getClass())
            ? new CallbackContext().getRetryAttempts() + 1
            : 1;
        verify(sdkClient, times(times)).updateApplication(any(UpdateApplicationRequest.class));
        assertThat(response).isNotNull();
        verify(sdkClient, never()).tagResource(any(TagResourceRequest.class));
        verify(sdkClient, never()).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(cfnException.getErrorCode());
        assertThat(response.getMessage()).containsSubsequence(cfnException.getMessage());
    }

    @Test
    public void handleRequest_InternalServerExceptionWhenUnTagging() {
        UpdateApplicationResponse updateApplicationResponse = updateApplicationResponse();
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, INITIAL_APPLICATION_TAGS));
        GetApplicationResponse postUpdateApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, DESIRED_APPLICATION_TAGS));

        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(updateApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse)
                .thenReturn(initialApplicationResponse)
                .thenReturn(postUpdateApplicationResponse);
        when(sdkClient.untagResource(any(UntagResourceRequest.class)))
                .thenThrow(InternalServerException.class);

        final ResourceModel model = ResourceModel.builder()
                .arn(APPLICATION_ARN)
                .applicationId(APPLICATION_ID)
                .tags(TagHelper.convertToSet(DESIRED_APPLICATION_TAGS))
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(DESIRED_APPLICATION_TAGS)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient, never()).tagResource(any(TagResourceRequest.class));
        verify(sdkClient, times(6)).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient, times(2)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @Test
    public void handleRequest_InternalServerExceptionWhenTagging() {
        UpdateApplicationResponse updateApplicationResponse = updateApplicationResponse();
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, INITIAL_APPLICATION_TAGS));
        GetApplicationResponse postUpdateApplicationResponse = getApplicationResponse(getApplication(ApplicationState.CREATED, DESIRED_APPLICATION_TAGS));

        when(sdkClient.updateApplication(any(UpdateApplicationRequest.class)))
                .thenReturn(updateApplicationResponse);
        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse)
                .thenReturn(initialApplicationResponse)
                .thenReturn(postUpdateApplicationResponse);
        when(sdkClient.tagResource(any(TagResourceRequest.class)))
                .thenThrow(InternalServerException.class);
        when(sdkClient.untagResource(any(UntagResourceRequest.class)))
                .thenReturn(UntagResourceResponse.builder().build());

        final ResourceModel model = ResourceModel.builder()
                .arn(APPLICATION_ARN)
                .applicationId(APPLICATION_ID)
                .tags(TagHelper.convertToSet(DESIRED_APPLICATION_TAGS))
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(DESIRED_APPLICATION_TAGS)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient, times(6)).tagResource(any(TagResourceRequest.class));
        verify(sdkClient).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient, times(2)).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @Test
    public void handleRequest_PreCheckFailureForTerminatedApplication() {
        GetApplicationResponse initialApplicationResponse = getApplicationResponse(getApplication(ApplicationState.TERMINATED, INITIAL_APPLICATION_TAGS));
        GetApplicationResponse postUpdateExpectedResponse = getApplicationResponse(getUpdatedApplication(ApplicationState.CREATED, DESIRED_APPLICATION_TAGS));

        when(sdkClient.getApplication(any(GetApplicationRequest.class)))
                .thenReturn(initialApplicationResponse);

        final ResourceModel model = Translator.translateFromReadResponse(postUpdateExpectedResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(DESIRED_APPLICATION_TAGS)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        verify(sdkClient, never()).updateApplication(any(UpdateApplicationRequest.class));
        verify(sdkClient, never()).tagResource(any(TagResourceRequest.class));
        verify(sdkClient, never()).untagResource(any(UntagResourceRequest.class));
        verify(sdkClient).getApplication(any(GetApplicationRequest.class));
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    private UpdateApplicationResponse updateApplicationResponse() {
        Application application = getApplication(APPLICATION_ID, ApplicationState.CREATED);
        return UpdateApplicationResponse.builder()
                .application(application)
                .build();
    }

    //Returns custom application with different values than the default application in AbstractTestBase class
    private Application getUpdatedApplication(ApplicationState state, Map<String, String> tags) {
        return getDefaultApplicationBuilder()
                .state(state)
                .autoStartConfiguration(AutoStartConfig.builder()
                        .enabled(true)
                        .build())
                .autoStopConfiguration(AutoStopConfig.builder()
                        .enabled(false)
                        .idleTimeoutMinutes(10)
                        .build())
                .initialCapacity(
                        ImmutableMap.<String, software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig>builder()
                                .put("DRIVER", software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig.builder()
                                        .workerCount(3L)
                                        .workerConfiguration(WorkerResourceConfig.builder()
                                                .cpu("update-driver-cpu")
                                                .disk("update-driver-disk")
                                                .memory("update-driver-memory")
                                                .build())
                                        .build())
                                .put("EXECUTOR", software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig.builder()
                                        .workerCount(5L)
                                        .workerConfiguration(WorkerResourceConfig.builder()
                                                .cpu("update-executor-cpu")
                                                .disk("update-executor-disk")
                                                .memory("update-executor-memory")
                                                .build())
                                        .build())
                                .build())
                .maximumCapacity(software.amazon.awssdk.services.emrserverless.model.MaximumAllowedResources.builder()
                        .cpu("update-max-cpu")
                        .disk("update-max-disk")
                        .memory("update-max-memory")
                        .build())
                .networkConfiguration(software.amazon.awssdk.services.emrserverless.model.NetworkConfiguration.builder()
                        .subnetIds(ImmutableSet.of("update-subnet-1", "update-subnet-2"))
                        .securityGroupIds(ImmutableSet.of("update-sg-1", "update-sg-2"))
                        .build())
                .tags(tags)
                .build();
    }

    /**
     * Returns service exceptions to CFN exceptions map for all the valid exceptions that can be thrown by UpdateApplication API.
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
                UPDATE_OPERATION, INTERNAL_SERVER_EXCEPTION))
            .build();
    }

}