package software.amazon.emrserverless.application;

import java.time.Instant;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.emrserverless.EmrServerlessClient;
import software.amazon.awssdk.services.emrserverless.model.Application;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.ApplicationSummary;
import software.amazon.awssdk.services.emrserverless.model.Architecture;
import software.amazon.awssdk.services.emrserverless.model.AutoStartConfig;
import software.amazon.awssdk.services.emrserverless.model.AutoStopConfig;
import software.amazon.awssdk.services.emrserverless.model.ConflictException;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.ImageConfiguration;
import software.amazon.awssdk.services.emrserverless.model.InternalServerException;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsResponse;
import software.amazon.awssdk.services.emrserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.emrserverless.model.ValidationException;
import software.amazon.awssdk.services.emrserverless.model.WorkerResourceConfig;
import software.amazon.awssdk.services.emrserverless.model.WorkerTypeSpecification;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

public abstract class AbstractTestBase {
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    protected static final String APPLICATION_ID = "TestApplicationId";
    protected static final String APPLICATION_ARN = "arn";
    protected static final String APPLICATION_NAME = "name";
    private static final String APPLICATION_TYPE = "SPARK";
    private static final String RELEASE_LABEL = "spark-6.5.0-preview";
    private static final String UPDATED_RELEASE_LABEL = "spark-6.10.0-preview";
    private static final Boolean AUTO_START_ENABLED = Boolean.FALSE;
    private static final Boolean AUTO_STOP_ENABLED = Boolean.TRUE;
    private static final Integer AUTO_STOP_IDLE_TIMEOUT = 300;
    private static final String DRIVER = "DRIVER";
    private static final String EXECUTOR = "EXECUTOR";
    private static final Long DRIVER_COUNT = 2L;
    private static final String DRIVER_CPU = "driver-cpu";
    private static final String DRIVER_DISK = "driver-disk";
    private static final String DRIVER_MEMORY = "driver-memory";
    private static final Long EXECUTOR_COUNT = 5L;
    private static final String EXECUTOR_CPU = "executor-cpu";
    private static final String EXECUTOR_DISK = "executor-disk";
    private static final String EXECUTOR_MEMORY = "executor-memory";
    private static final String MAX_CPU = "max-cpu";
    private static final String MAX_DISK = "max-disk";
    private static final String MAX_MEMORY = "max-memory";
    private static final String CUSTOM_DISK_TYPE = "SHUFFLE_OPTIMIZED";
    private static final Set<String> SUBNETS = ImmutableSet.of("subnet-1", "subnet-2", "subnet-3");
    private static final Set<String> SECURITY_GROUPS =
        ImmutableSet.of("sg-1", "sg-2", "sg-3", "sg-4", "sg-5");
    protected static final Map<String, String> APPLICATION_TAGS = ImmutableMap.of(
        "tag-key-1", "tag-value-1",
        "tag-key-2", "tag-value-2",
        "tag-key-3", "tag-value-3"
    );
    protected static final Map<String, String> RUNTIME_CONFIGURATION_PROPERTIES = ImmutableMap.of(
            "property-key-1", "property-value-1",
            "property-key-2", "property-value-2",
            "property-key-3", "property-value-3"
    );
    private static final Instant APPLICATION_CREATED_AT = Instant.now().minus(Period.ofDays(10));
    private static final Instant APPLICATION_UPDATED_AT = Instant.now().minus(Period.ofDays(2));
    private static final ApplicationState APPLICATION_STATE = ApplicationState.STARTED;
    private static final String APPLICATION_STATE_DETAILS = "state-details";
    protected static final String NEXT_TOKEN_1 = "NextToken1";
    protected static final String NEXT_TOKEN_2 = "NextToken2";

    protected static final ValidationException VALIDATION_EXCEPTION = ValidationException.builder()
        .message("validation exception").build();
    protected static final ResourceNotFoundException NOT_FOUND_EXCEPTION = ResourceNotFoundException.builder()
        .message("application does not exist").build();
    protected static final InternalServerException INTERNAL_SERVER_EXCEPTION = InternalServerException.builder()
        .message("internal server error").build();
    protected static final ConflictException CONFLICT_EXCEPTION = ConflictException.builder()
        .message("conflict exception").build();
    protected static final Architecture ARCHITECTURE = Architecture.X86_64;
    protected static final String IMAGE_URI = "image uri";
    protected static final String IMAGE_DIGEST = "image digest";
    protected static final String WORKER_TYPE = "worker type";

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    protected GetApplicationResponse getApplicationResponse(Application application) {
        return GetApplicationResponse.builder()
            .application(application)
            .build();
    }

    protected Application getApplication(String applicationId) {
        return getDefaultApplicationBuilder()
            .applicationId(applicationId)
            .build();
    }

    protected Application getApplication(ApplicationState state, Map<String, String> tags) {
        return getDefaultApplicationBuilder()
            .state(state)
            .tags(tags)
            .build();
    }

    protected Application getApplicationWithUpdatedReleaseLabel(ApplicationState state, Map<String, String> tags) {
        return getDefaultApplicationBuilder()
                .releaseLabel(UPDATED_RELEASE_LABEL)
                .state(state)
                .tags(tags)
                .build();
    }

    protected Application.Builder getDefaultApplicationBuilder() {
        Map<String, WorkerTypeSpecification> workerTypeSpecificationMap = new HashMap<>();
        workerTypeSpecificationMap.put(
            WORKER_TYPE, WorkerTypeSpecification.builder()
                .imageConfiguration(ImageConfiguration.builder()
                    .imageUri(IMAGE_URI)
                    .resolvedImageDigest(IMAGE_DIGEST)
                    .build())
                .build()
        );

        return Application.builder()
                .applicationId(APPLICATION_ID)
                .arn(APPLICATION_ARN)
                .name(APPLICATION_NAME)
                .type(APPLICATION_TYPE)
                .releaseLabel(RELEASE_LABEL)
                .architecture(ARCHITECTURE)
                .autoStartConfiguration(AutoStartConfig.builder()
                        .enabled(AUTO_START_ENABLED)
                        .build())
                .autoStopConfiguration(AutoStopConfig.builder()
                        .enabled(AUTO_STOP_ENABLED)
                        .idleTimeoutMinutes(AUTO_STOP_IDLE_TIMEOUT)
                        .build())
                .initialCapacity(
                        ImmutableMap.<String, software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig>builder()
                                .put(DRIVER, software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig.builder()
                                        .workerCount(DRIVER_COUNT)
                                        .workerConfiguration(WorkerResourceConfig.builder()
                                                .cpu(DRIVER_CPU)
                                                .disk(DRIVER_DISK)
                                                .diskType(CUSTOM_DISK_TYPE)
                                                .memory(DRIVER_MEMORY)
                                                .build())
                                        .build())
                                .put(EXECUTOR, software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig.builder()
                                        .workerCount(EXECUTOR_COUNT)
                                        .workerConfiguration(WorkerResourceConfig.builder()
                                                .cpu(EXECUTOR_CPU)
                                                .disk(EXECUTOR_DISK)
                                                .diskType(CUSTOM_DISK_TYPE)
                                                .memory(EXECUTOR_MEMORY)
                                                .build())
                                        .build())
                                .build())
                .imageConfiguration(software.amazon.awssdk.services.emrserverless.model.ImageConfiguration.builder()
                        .imageUri(IMAGE_URI)
                        .resolvedImageDigest(IMAGE_DIGEST)
                        .build())
                .interactiveConfiguration(software.amazon.awssdk.services.emrserverless.model.InteractiveConfiguration.builder()
                        .studioEnabled(Boolean.FALSE)
                        .livyEndpointEnabled(Boolean.TRUE)
                        .build())
                .monitoringConfiguration(software.amazon.awssdk.services.emrserverless.model.MonitoringConfiguration.builder()
                        .s3MonitoringConfiguration(software.amazon.awssdk.services.emrserverless.model.S3MonitoringConfiguration
                                .builder().encryptionKeyArn("ENCRYPTION_KEY")
                                .logUri("s3://98")
                                .build())
                        .cloudWatchLoggingConfiguration(software.amazon.awssdk.services.emrserverless.model
                                .CloudWatchLoggingConfiguration.builder()
                                .enabled(Boolean.TRUE)
                                .logGroupName("logGroup")
                                .logStreamNamePrefix("logStreamPrefix")
                                .logTypes(Map.of("SPARK_DRIVER", List.of("STDERR", "STDOUT")))
                                .encryptionKeyArn("ENCRYPTION_KEY").build())
                        .managedPersistenceMonitoringConfiguration(software.amazon.awssdk.services.emrserverless.model
                                .ManagedPersistenceMonitoringConfiguration.builder()
                                .enabled(Boolean.TRUE)
                                .encryptionKeyArn("ENCRYPTION_KEY").build())
                        .build())
                .runtimeConfiguration(Sets.newHashSet(software.amazon.awssdk.services.emrserverless.model.Configuration.builder()
                        .classification("SPARK")
                        .properties(RUNTIME_CONFIGURATION_PROPERTIES)
                        .configurations(Lists.newArrayList())
                        .build()))
                .maximumCapacity(software.amazon.awssdk.services.emrserverless.model.MaximumAllowedResources.builder()
                        .cpu(MAX_CPU)
                        .disk(MAX_DISK)
                        .memory(MAX_MEMORY)
                        .build())
                .networkConfiguration(software.amazon.awssdk.services.emrserverless.model.NetworkConfiguration.builder()
                        .subnetIds(SUBNETS)
                        .securityGroupIds(SECURITY_GROUPS)
                        .build())
                .tags(APPLICATION_TAGS)
                //Read only settings
                .state(APPLICATION_STATE)
                .stateDetails(APPLICATION_STATE_DETAILS)
                .createdAt(APPLICATION_CREATED_AT)
                .updatedAt(APPLICATION_UPDATED_AT)
                .workerTypeSpecifications(workerTypeSpecificationMap);
    }

    protected ListApplicationsResponse getListApplicationsResponse() {
        return ListApplicationsResponse.builder()
            .applications(Lists.newArrayList(getApplicationSummary()))
            .nextToken(NEXT_TOKEN_2)
            .build();
    }

    private ApplicationSummary getApplicationSummary() {
        return ApplicationSummary.builder()
            .id(APPLICATION_ID)
            .arn(APPLICATION_ARN)
            .name(APPLICATION_NAME)
            .build();
    }

    protected Application getApplication(String applicationId, ApplicationState state) {
        return getDefaultApplicationBuilder()
            .applicationId(applicationId)
            .state(state)
            .build();
    }

    protected ResourceModel getResourceModel(String applicationId, Map<String, String> tags, String releaseLabel) {
        Map<String, WorkerTypeSpecificationInput> workerTypeSpecificationInputMap = new HashMap<>();
        workerTypeSpecificationInputMap.put(
            WORKER_TYPE, WorkerTypeSpecificationInput.builder()
                .imageConfiguration(ImageConfigurationInput.builder()
                    .imageUri(IMAGE_URI)
                    .build())
                .build()
        );

        return ResourceModel.builder()
                .applicationId(applicationId)
                .name(APPLICATION_NAME)
                .type(APPLICATION_TYPE)
                .arn(APPLICATION_ARN)
                .releaseLabel(releaseLabel)
                .architecture(ARCHITECTURE.name())
                .autoStartConfiguration(AutoStartConfiguration.builder()
                        .enabled(AUTO_START_ENABLED)
                        .build())
                .autoStopConfiguration(AutoStopConfiguration.builder()
                        .enabled(AUTO_STOP_ENABLED)
                        .idleTimeoutMinutes(AUTO_STOP_IDLE_TIMEOUT)
                        .build())
                .imageConfiguration(ImageConfigurationInput.builder()
                        .imageUri(IMAGE_URI)
                        .build())
                .interactiveConfiguration(InteractiveConfiguration.builder()
                        .studioEnabled(Boolean.FALSE)
                        .livyEndpointEnabled(Boolean.TRUE)
                        .build())
                .monitoringConfiguration(MonitoringConfiguration
                        .builder()
                        .s3MonitoringConfiguration(software.amazon.emrserverless.application.S3MonitoringConfiguration
                                .builder()
                                .encryptionKeyArn("ENCRYPTION_KEY")
                                .logUri("s3://98")
                                .build())
                        .cloudWatchLoggingConfiguration(software.amazon.emrserverless.application
                                .CloudWatchLoggingConfiguration.builder()
                                .enabled(Boolean.TRUE)
                                .logGroupName("logGroup")
                                .logTypeMap(Set.of(LogTypeMapKeyValuePair.builder()
                                        .key("SPARK_DRIVER")
                                        .value(Set.of("STDERR", "STDOUT"))
                                        .build()))
                                .logStreamNamePrefix("logStreamPrefix")
                                .encryptionKeyArn("ENCRYPTION_KEY").build())
                        .managedPersistenceMonitoringConfiguration(software.amazon.emrserverless.application.ManagedPersistenceMonitoringConfiguration
                                .builder()
                                .enabled(Boolean.TRUE)
                                .encryptionKeyArn("ENCRYPTION_KEY").build())
                        .build())
                .runtimeConfiguration(Sets.newHashSet(software.amazon.emrserverless.application.ConfigurationObject.builder()
                        .classification("SPARK")
                        .properties(RUNTIME_CONFIGURATION_PROPERTIES)
                        .configurations(Sets.newHashSet())
                        .build()))
                .initialCapacity(Sets.newHashSet(
                        InitialCapacityConfigKeyValuePair.builder()
                                .key(DRIVER)
                                .value(InitialCapacityConfig.builder()
                                        .workerCount(DRIVER_COUNT)
                                        .workerConfiguration(WorkerConfiguration.builder()
                                                .cpu(DRIVER_CPU)
                                                .disk(DRIVER_DISK)
                                                .diskType(CUSTOM_DISK_TYPE)
                                                .memory(DRIVER_MEMORY)
                                                .build())
                                        .build())
                                .build(),
                        InitialCapacityConfigKeyValuePair.builder()
                                .key(EXECUTOR)
                                .value(InitialCapacityConfig.builder()
                                        .workerCount(EXECUTOR_COUNT)
                                        .workerConfiguration(WorkerConfiguration.builder()
                                                .cpu(EXECUTOR_CPU)
                                                .disk(EXECUTOR_DISK)
                                                .diskType(CUSTOM_DISK_TYPE)
                                                .memory(EXECUTOR_MEMORY)
                                                .build())
                                        .build())
                                .build()
                ))
                .maximumCapacity(MaximumAllowedResources.builder()
                        .cpu(MAX_CPU)
                        .disk(MAX_DISK)
                        .memory(MAX_MEMORY)
                        .build())
                .networkConfiguration(NetworkConfiguration.builder()
                        .subnetIds(SUBNETS)
                        .securityGroupIds(SECURITY_GROUPS)
                        .build())
                .tags(tags.entrySet().stream()
                        .map(entry -> Tag.builder()
                                .key(entry.getKey())
                                .value(entry.getValue())
                                .build())
                        .collect(Collectors.toSet()))
                .workerTypeSpecifications(workerTypeSpecificationInputMap)
                .build();
    }

    protected ResourceModel getResourceModel(String applicationId, Map<String, String> tags) {
        return getResourceModel(applicationId, tags, RELEASE_LABEL);
    }

    protected ResourceModel getResourceModel(String applicationId) {
        return getResourceModel(applicationId, APPLICATION_TAGS);
    }

    public Stream<Arguments> exceptionArgumentsProvider() {
        return getCFNExceptionMapping().entrySet()
            .stream()
            .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    protected abstract Map<Exception, BaseHandlerException> getCFNExceptionMapping();

    static ProxyClient<EmrServerlessClient> MOCK_PROXY(
        final AmazonWebServicesClientProxy proxy,
        final EmrServerlessClient sdkClient) {
        return new ProxyClient<EmrServerlessClient>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
            injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
            IterableT
            injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
            injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
            injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public EmrServerlessClient client() {
                return sdkClient;
            }
        };
    }
}