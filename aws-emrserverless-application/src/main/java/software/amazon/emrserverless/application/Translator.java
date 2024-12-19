package software.amazon.emrserverless.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

import software.amazon.awssdk.services.emrserverless.model.Application;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.Architecture;
import software.amazon.awssdk.services.emrserverless.model.ConflictException;
import software.amazon.awssdk.services.emrserverless.model.CreateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.DeleteApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.EmrServerlessException;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.ImageConfiguration;
import software.amazon.awssdk.services.emrserverless.model.ImageConfigurationInput;
import software.amazon.awssdk.services.emrserverless.model.InternalServerException;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsRequest;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsResponse;
import software.amazon.awssdk.services.emrserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.emrserverless.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.emrserverless.model.TagResourceRequest;
import software.amazon.awssdk.services.emrserverless.model.UntagResourceRequest;
import software.amazon.awssdk.services.emrserverless.model.UpdateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.ValidationException;
import software.amazon.awssdk.services.emrserverless.model.WorkerTypeSpecification;
import software.amazon.awssdk.services.emrserverless.model.WorkerTypeSpecificationInput;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static software.amazon.emrserverless.application.BaseHandlerStd.ACCESS_DENIED_ERROR_CODE;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Translate from resource model's InitialCapacityConfig key-value pairs to service sdk model's InitialCapacityConfig map.
     *
     * @param initialCapacityConfigs Sets of resource model's InitialCapacityConfigKeyValuePair objects
     * @return Service sdk model's InitialCapacityConfig object map
     */
    static Map<String, software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig> translate(
        final Set<InitialCapacityConfigKeyValuePair> initialCapacityConfigs) {
        return streamOfOrEmpty(initialCapacityConfigs)
            .collect(Collectors.toMap(
                InitialCapacityConfigKeyValuePair::getKey,
                entry -> translate(entry.getValue())
            ));
    }

    /**
     * Translate resource model's InitialCapacityConfig to service sdk model's InitialCapacityConfig.
     *
     * @param initialCapacityConfig Resource model's InitialCapacityConfig object
     * @return Service sdk model's InitialCapacityConfig object
     */
    static software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig translate(
        final InitialCapacityConfig initialCapacityConfig) {
        return Optional.ofNullable(initialCapacityConfig)
            .map(config -> software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig.builder()
                .workerCount(config.getWorkerCount())
                .workerConfiguration(translate(config.getWorkerConfiguration()))
                .build())
            .orElse(null);
    }

    /**
     * Translate resource model's WorkerResourceConfig to service sdk model's WorkerResourceConfig.
     *
     * @param workerResourceConfig Resource model's WorkerResourceConfig object
     * @return Service sdk model's WorkerResourceConfig object
     */
    static software.amazon.awssdk.services.emrserverless.model.WorkerResourceConfig translate(
        final WorkerConfiguration workerResourceConfig) {
        return Optional.ofNullable(workerResourceConfig)
            .map(config -> software.amazon.awssdk.services.emrserverless.model.WorkerResourceConfig.builder()
                .cpu(config.getCpu())
                .memory(config.getMemory())
                .disk(config.getDisk())
                .diskType(config.getDiskType())
                .build())
            .orElse(null);
    }

    /**
     * Translate resource model's MaximumAllowedResources to service sdk model's MaximumAllowedResources.
     *
     * @param maximumCapacity Resource model's MaximumAllowedResources object
     * @return Service sdk model's MaximumAllowedResources object
     */
    static software.amazon.awssdk.services.emrserverless.model.MaximumAllowedResources translate(
        final MaximumAllowedResources maximumCapacity) {
        return Optional.ofNullable(maximumCapacity)
            .map(maxCapacity -> software.amazon.awssdk.services.emrserverless.model.MaximumAllowedResources.builder()
                .cpu(maxCapacity.getCpu())
                .memory(maxCapacity.getMemory())
                .disk(maxCapacity.getDisk())
                .build())
            .orElse(null);
    }

    /**
     * Translate resource model's AutoStartConfig to service sdk model's AutoStartConfig.
     *
     * @param autoStartConfig Resource model's AutoStartConfig object
     * @return Service sdk model's AutoStartConfig object
     */
    static software.amazon.awssdk.services.emrserverless.model.AutoStartConfig translate(
        final AutoStartConfiguration autoStartConfig) {
        return Optional.ofNullable(autoStartConfig)
            .map(config -> software.amazon.awssdk.services.emrserverless.model.AutoStartConfig.builder()
                .enabled(config.getEnabled())
                .build())
            .orElse(null);
    }

    /**
     * Translate resource model's AutoStopConfiguration to service sdk model's AutoStopConfiguration.
     *
     * @param autoStopConfig Resource model's AutoStopConfiguration object
     * @return Service sdk model's AutoStopConfiguration object
     */
    static software.amazon.awssdk.services.emrserverless.model.AutoStopConfig translate(
        final AutoStopConfiguration autoStopConfig) {
        return Optional.ofNullable(autoStopConfig)
            .map(config -> software.amazon.awssdk.services.emrserverless.model.AutoStopConfig.builder()
                .enabled(config.getEnabled())
                .idleTimeoutMinutes(config.getIdleTimeoutMinutes())
                .build())
            .orElse(null);
    }

    /**
     * Translate resource model's NetworkConfiguration to service sdk model's NetworkConfiguration.
     *
     * @param networkConfiguration Resource model's NetworkConfiguration object
     * @return Service sdk model's NetworkConfiguration object
     */
    static software.amazon.awssdk.services.emrserverless.model.NetworkConfiguration translate(
        final NetworkConfiguration networkConfiguration) {
        return Optional.ofNullable(networkConfiguration)
            .map(config -> software.amazon.awssdk.services.emrserverless.model.NetworkConfiguration.builder()
                .subnetIds(config.getSubnetIds())
                .securityGroupIds(config.getSecurityGroupIds())
                .build())
            .orElse(null);
    }

    /**
     * Translate from service sdk's Application object to application resource model.
     *
     * @param sdkApplication Service sdk's Application object
     * @return Application resource model
     */
    static ResourceModel translate(final Application sdkApplication) {
        return Optional.ofNullable(sdkApplication)
                .map(application -> ResourceModel.builder()
                        .applicationId(application.applicationId())
                        .arn(application.arn())
                        .name(application.name())
                        .type(application.type())
                        .releaseLabel(application.releaseLabel())
                        .initialCapacity(translate(application.initialCapacity()))
                        .maximumCapacity(translate(application.maximumCapacity()))
                        .autoStartConfiguration(translate(application.autoStartConfiguration()))
                        .autoStopConfiguration(translate(application.autoStopConfiguration()))
                        .networkConfiguration(translate(application.networkConfiguration()))
                        .tags(TagHelper.convertToSet(application.tags()))
                        .architecture(application.architectureAsString())
                        .imageConfiguration(translate(application.imageConfiguration()))
                        .workerTypeSpecifications(translateToRead(application.workerTypeSpecifications()))
                        .monitoringConfiguration(translate(application.monitoringConfiguration()))
                        .runtimeConfiguration(translate(application.runtimeConfiguration()))
                        .interactiveConfiguration(translate(application.interactiveConfiguration()))
                        .schedulerConfiguration(translate(application.schedulerConfiguration()))
                        .build())
                .orElse(null);
    }

    private static SchedulerConfiguration translate(software.amazon.awssdk.services.emrserverless.model.SchedulerConfiguration schedulerConfiguration) {
        return schedulerConfiguration == null ? null : SchedulerConfiguration.builder()
            .queueTimeoutMinutes(schedulerConfiguration.queueTimeoutMinutes() == null ? null : schedulerConfiguration.queueTimeoutMinutes())
            .maxConcurrentRuns(schedulerConfiguration.maxConcurrentRuns() == null ? null : schedulerConfiguration.maxConcurrentRuns())
            .build();  
    }

    private static Map<String, software.amazon.emrserverless.application.WorkerTypeSpecificationInput> translateToRead(
        Map<String, WorkerTypeSpecification> stringWorkerTypeSpecificationMap) {

        if (stringWorkerTypeSpecificationMap == null) {
            return null;
        }

        return stringWorkerTypeSpecificationMap.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> software.amazon.emrserverless.application.WorkerTypeSpecificationInput.builder()
                    .imageConfiguration(translate(entry.getValue().imageConfiguration()))
                    .build()));
    }

    private static software.amazon.emrserverless.application.ImageConfigurationInput translate(ImageConfiguration imageConfiguration) {
        return imageConfiguration == null
            ? null
            : software.amazon.emrserverless.application.ImageConfigurationInput.builder()
            .imageUri(imageConfiguration.imageUri())
            .build();
    }

    private static InteractiveConfiguration translate(software.amazon.awssdk.services.emrserverless.model.InteractiveConfiguration interactiveConfiguration) {
        return interactiveConfiguration == null
                ? null
                : InteractiveConfiguration.builder()
                .studioEnabled(interactiveConfiguration.studioEnabled() == null ? null : interactiveConfiguration.studioEnabled())
                .livyEndpointEnabled(interactiveConfiguration.livyEndpointEnabled() == null ? null : interactiveConfiguration.livyEndpointEnabled())
                .build();
    }

    private static MonitoringConfiguration translate(software.amazon.awssdk.services.emrserverless.model.MonitoringConfiguration monitoringConfiguration) {
        return monitoringConfiguration == null
                ? null
                : MonitoringConfiguration.builder()
                .s3MonitoringConfiguration(translate(monitoringConfiguration.s3MonitoringConfiguration()))
                .managedPersistenceMonitoringConfiguration(translate(monitoringConfiguration.managedPersistenceMonitoringConfiguration()))
                .cloudWatchLoggingConfiguration(translate(monitoringConfiguration.cloudWatchLoggingConfiguration()))
                .build();
    }

    private static S3MonitoringConfiguration translate(software.amazon.awssdk.services.emrserverless.model.S3MonitoringConfiguration s3MonitoringConfiguration) {
        return s3MonitoringConfiguration == null
                ? null
                : S3MonitoringConfiguration.builder()
                .logUri(s3MonitoringConfiguration.logUri() == null ? null : s3MonitoringConfiguration.logUri())
                .encryptionKeyArn(s3MonitoringConfiguration.encryptionKeyArn() == null ? null : s3MonitoringConfiguration.encryptionKeyArn())
                .build();
    }

    private static ManagedPersistenceMonitoringConfiguration translate(software.amazon.awssdk.services.emrserverless.model.ManagedPersistenceMonitoringConfiguration managedPersistenceMonitoringConfiguration) {
        return managedPersistenceMonitoringConfiguration == null
                ? null
                : ManagedPersistenceMonitoringConfiguration.builder()
                .enabled(managedPersistenceMonitoringConfiguration.enabled() == null ? null : managedPersistenceMonitoringConfiguration.enabled())
                .encryptionKeyArn(managedPersistenceMonitoringConfiguration.encryptionKeyArn() == null ? null : managedPersistenceMonitoringConfiguration.encryptionKeyArn())
                .build();
    }

    private static CloudWatchLoggingConfiguration translate(software.amazon.awssdk.services.emrserverless.model.CloudWatchLoggingConfiguration cloudWatchLoggingConfiguration) {
        return cloudWatchLoggingConfiguration == null
                ? null
                : CloudWatchLoggingConfiguration.builder()
                .enabled(cloudWatchLoggingConfiguration.enabled() == null ? null : cloudWatchLoggingConfiguration.enabled())
                .encryptionKeyArn(cloudWatchLoggingConfiguration.encryptionKeyArn() == null ? null : cloudWatchLoggingConfiguration.encryptionKeyArn())
                .logGroupName(cloudWatchLoggingConfiguration.logGroupName() == null ? null : cloudWatchLoggingConfiguration.logGroupName())
                .logStreamNamePrefix(cloudWatchLoggingConfiguration.logStreamNamePrefix() == null ? null : cloudWatchLoggingConfiguration.logStreamNamePrefix())
                .logTypeMap(cloudWatchLoggingConfiguration.logTypes() == null ? null : translateMap(cloudWatchLoggingConfiguration.logTypes()))
                .build();
    }

    private static Set<LogTypeMapKeyValuePair> translateMap(Map<String, List<String>> logTypeMap) {
        return logTypeMap == null
                ? null
                : logTypeMap.entrySet()
                .stream()
                .map(entry -> {
                    if (entry.getKey() == null || entry.getValue() == null) {
                        return null;
                    }
                    return LogTypeMapKeyValuePair.builder()
                            .key(entry.getKey())
                            .value(entry.getValue().stream().collect(Collectors.toSet()))
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }



    private static Set<ConfigurationObject> translate(List<software.amazon.awssdk.services.emrserverless.model.Configuration> configurationList) {
        return configurationList == null
                ? null
                : configurationList.stream().map(Translator::translate)
                .collect(Collectors.toSet());
    }

    private static ConfigurationObject translate(software.amazon.awssdk.services.emrserverless.model.Configuration configuration) {
        List<ConfigurationObject> configurationList = configuration.configurations() == null ? null : configuration.configurations()
                .stream().map(Translator::translate)
                .collect(Collectors.toList());
        return ConfigurationObject.builder()
                .classification(configuration.classification() == null ? null : configuration.classification())
                .properties(configuration.properties() == null ? null : configuration.properties())
                .configurations(configurationList == null ? null : configurationList.stream().collect(Collectors.toSet()))
                .build();
    }

    /**
     * Translate from service sdk model's InitialCapacityConfig map to service sdk model's InitialCapacityConfigKeyValuePair set.
     *
     * @param initialCapacityConfigs Service sdk model's InitialCapacityConfig object map
     * @return Set of resource model's InitialCapacityConfigKeyValuePair objects
     */
    static Set<InitialCapacityConfigKeyValuePair> translate(
        final Map<String, software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig> initialCapacityConfigs) {
        return Optional.ofNullable(initialCapacityConfigs)
            .map(Map::entrySet)
            .map(entrySet -> entrySet.stream()
                .map(entry -> InitialCapacityConfigKeyValuePair.builder()
                    .key(entry.getKey())
                    .value(translate(entry.getValue()))
                    .build())
                .collect(Collectors.toSet()))
            .orElse(null);
    }

    /**
     * Translate service sdk model's InitialCapacityConfig to resource model's InitialCapacityConfig.
     *
     * @param initialCapacityConfig Service sdk model's InitialCapacityConfig object
     * @return Resource model's InitialCapacityConfig object
     */

    static InitialCapacityConfig translate(
        final software.amazon.awssdk.services.emrserverless.model.InitialCapacityConfig initialCapacityConfig) {
        return Optional.ofNullable(initialCapacityConfig)
            .map(config -> InitialCapacityConfig.builder()
                .workerCount(config.workerCount())
                .workerConfiguration(translate(config.workerConfiguration()))
                .build())
            .orElse(null);
    }

    /**
     * Translate service sdk model's WorkerResourceConfig to resource model's WorkerResourceConfig.
     *
     * @param workerResourceConfig Service sdk model's WorkerResourceConfig object
     * @return Resource model's WorkerResourceConfig object
     */
    static WorkerConfiguration translate(
        final software.amazon.awssdk.services.emrserverless.model.WorkerResourceConfig workerResourceConfig) {
        return Optional.ofNullable(workerResourceConfig)
            .map(config -> WorkerConfiguration.builder()
                .cpu(config.cpu())
                .memory(config.memory())
                .disk(config.disk())
                .diskType(config.diskType())
                .build())
            .orElse(null);
    }

    /**
     * Translate service sdk model's MaximumAllowedResources to resource model's MaximumAllowedResources.
     *
     * @param maximumCapacity Service sdk model's MaximumAllowedResources object
     * @return Resource model's MaximumAllowedResources object
     */
    static MaximumAllowedResources translate(
        final software.amazon.awssdk.services.emrserverless.model.MaximumAllowedResources maximumCapacity) {
        return Optional.ofNullable(maximumCapacity)
            .map(maxCapacity -> MaximumAllowedResources.builder()
                .cpu(maxCapacity.cpu())
                .memory(maxCapacity.memory())
                .disk(maxCapacity.disk())
                .build())
            .orElse(null);
    }

    /**
     * Translate resource model's AutoStartConfiguration to service sdk model's AutoStartConfiguration.
     *
     * @param autoStartConfig Resource model's AutoStartConfiguration object
     * @return Service sdk model's AutoStartConfiguration object
     */
    static AutoStartConfiguration translate(
        final software.amazon.awssdk.services.emrserverless.model.AutoStartConfig autoStartConfig) {
        return Optional.ofNullable(autoStartConfig)
            .map(config -> AutoStartConfiguration.builder()
                .enabled(config.enabled())
                .build())
            .orElse(null);
    }

    /**
     * Translate resource model's AutoStopConfiguration to service sdk model's AutoStopConfiguration.
     *
     * @param autoStopConfig Resource model's AutoStopConfiguration object
     * @return Service sdk model's AutoStopConfiguration object
     */
    static AutoStopConfiguration translate(
        final software.amazon.awssdk.services.emrserverless.model.AutoStopConfig autoStopConfig) {
        return Optional.ofNullable(autoStopConfig)
            .map(config -> AutoStopConfiguration.builder()
                .enabled(config.enabled())
                .idleTimeoutMinutes(config.idleTimeoutMinutes())
                .build())
            .orElse(null);
    }

    /**
     * Translate service sdk model's NetworkConfiguration to resource model's NetworkConfiguration.
     *
     * @param networkConfiguration Service sdk model's NetworkConfiguration object
     * @return Resource model's NetworkConfiguration object
     */
    static NetworkConfiguration translate(
        final software.amazon.awssdk.services.emrserverless.model.NetworkConfiguration networkConfiguration) {
        return Optional.ofNullable(networkConfiguration)
            .map(config -> NetworkConfiguration.builder()
                .subnetIds(Optional.ofNullable(config.subnetIds())
                    .map(Sets::newHashSet)
                    .orElse(null))
                .securityGroupIds(Optional.ofNullable(config.securityGroupIds())
                    .map(Sets::newHashSet)
                    .orElse(null))
                .build())
            .orElse(null);
    }

    /**
     * Translate emr-serverless exceptions to cloud-formation handler exceptions.
     *
     * @param exception  Emr-serverless exception
     * @param operation  Operation for which the exception is thrown.
     * @param resourceId Resource identifier for which the operation is invoked.
     * @return Translated cfn handler exception
     */
    static BaseHandlerException translate(final EmrServerlessException exception, final String operation, final String resourceId,
                                          final CallbackContext callbackContext) {
        return Optional.ofNullable(exception)
            .map(e -> {
                if (e instanceof ValidationException) {
                    return new CfnInvalidRequestException(e.getMessage(), e);
                } else if (e instanceof ResourceNotFoundException) {
                    return StringUtils.isEmpty(resourceId)
                        ? new CfnNotFoundException(e)
                        : new CfnNotFoundException(ResourceModel.TYPE_NAME, resourceId);
                } else if (e instanceof InternalServerException && callbackContext.isStabilizationFailed()) {
                    return new CfnNotStabilizedException(ResourceModel.TYPE_NAME, resourceId, e);
                } else if (e instanceof InternalServerException) {
                    return new CfnServiceInternalErrorException(operation, e);
                } else if (e instanceof ServiceQuotaExceededException) {
                    return new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.getMessage(), e);
                } else if (e instanceof ConflictException) {
                    return StringUtils.isEmpty(resourceId)
                        ? new CfnResourceConflictException(e)
                        : new CfnResourceConflictException(ResourceModel.TYPE_NAME, resourceId, e.getMessage(), e);
                } else if (StringUtils.equals(ACCESS_DENIED_ERROR_CODE,  getErrorCode(e))) {
                    return new CfnAccessDeniedException(ResourceModel.TYPE_NAME, e);
                } else {
                    return new CfnGeneralServiceException(operation, e);
                }
            })
            .orElse(null);
    }

    private static String getErrorCode(Exception e) {
        if (e instanceof EmrServerlessException && ((EmrServerlessException) e).awsErrorDetails() != null)
        {
            return ((EmrServerlessException) e).awsErrorDetails().errorCode();
        }
        return e.getMessage();
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }


    /**
     * Request to create an Application.
     *
     * @param model resource model
     * @return CreateApplicationRequest the aws service request to create a resource
     */
    static CreateApplicationRequest translateToCreateRequest(final ResourceModel model, ResourceHandlerRequest<ResourceModel> request) {
        return CreateApplicationRequest.builder()
                .releaseLabel(model.getReleaseLabel())
                .clientToken(request.getClientRequestToken())
                .type(model.getType())
                .name(model.getName())
                .autoStartConfiguration(translate(model.getAutoStartConfiguration()))
                .autoStopConfiguration(translate(model.getAutoStopConfiguration()))
                .initialCapacity(translate(model.getInitialCapacity()))
                .maximumCapacity(translate(model.getMaximumCapacity()))
                .networkConfiguration(translate(model.getNetworkConfiguration()))
                .tags(TagHelper.generateTagsForCreate(model, request))
                .architecture(translate(model.getArchitecture()))
                .imageConfiguration(translate(model.getImageConfiguration()))
                .workerTypeSpecifications(translateToWorkerTypeSpecMap(model.getWorkerTypeSpecifications()))
                .monitoringConfiguration(translate(model.getMonitoringConfiguration()))
                .runtimeConfiguration(model.getRuntimeConfiguration() == null ? null:
                        model.getRuntimeConfiguration().stream().map(Translator::translate)
                                .collect(Collectors.toList()))
                .interactiveConfiguration(translate(model.getInteractiveConfiguration()))
                .schedulerConfiguration(translate(model.getSchedulerConfiguration()))
                .build();
    }

    private static Architecture translate(String architecture) {
        return architecture == null
            ? null
            : Architecture.valueOf(architecture);
    }

    private static Map<String, WorkerTypeSpecificationInput> translateToWorkerTypeSpecMap(
        Map<String, software.amazon.emrserverless.application.WorkerTypeSpecificationInput> workerTypeSpecifications) {

        if (workerTypeSpecifications == null) {
            return null;
        }

        return workerTypeSpecifications.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> WorkerTypeSpecificationInput.builder()
                    .imageConfiguration(translate(entry.getValue().getImageConfiguration()))
                    .build()
            ));
    }

    private static ImageConfigurationInput translate(software.amazon.emrserverless.application.ImageConfigurationInput imageConfiguration) {
        return imageConfiguration == null
            ? null
            : ImageConfigurationInput.builder()
            .imageUri(imageConfiguration.getImageUri())
            .build();
    }

    /**
     * Request to read a resource.
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static GetApplicationRequest translateToReadRequest(final ResourceModel model) {
        return GetApplicationRequest.builder()
            .applicationId(model.getApplicationId())
            .build();
    }

    /**
     * Translates resource object from sdk into a resource model.
     *
     * @param response the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final GetApplicationResponse response) {
        return Optional.ofNullable(response)
            .map(GetApplicationResponse::application)
            .map(Translator::translate)
            .orElse(null);
    }

    /**
     * Request to delete a resource.
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteApplicationRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteApplicationRequest.builder()
            .applicationId(model.getApplicationId())
            .build();
    }

    /**
     * Request to update properties of a previously created resource.
     *
     * @param model   resource model
     * @param request request
     * @return UpdateApplicationRequest the aws service request to create a resource
     */
    static UpdateApplicationRequest translateToUpdateRequest(final ResourceModel model,
                                                             final ResourceHandlerRequest<ResourceModel> request) {
        return UpdateApplicationRequest.builder()
                .applicationId(model.getApplicationId())
                .releaseLabel(model.getReleaseLabel())
                .clientToken(request.getClientRequestToken())
                .autoStartConfiguration(translate(model.getAutoStartConfiguration()))
                .autoStopConfiguration(translate(model.getAutoStopConfiguration()))
                .initialCapacity(translate(model.getInitialCapacity()))
                .maximumCapacity(translate(model.getMaximumCapacity()))
                .networkConfiguration(translate(model.getNetworkConfiguration()))
                .architecture(translate(model.getArchitecture()))
                .imageConfiguration(translate(model.getImageConfiguration()))
                .workerTypeSpecifications(translateToWorkerTypeSpecMap(model.getWorkerTypeSpecifications()))
                .monitoringConfiguration(translate(model.getMonitoringConfiguration()))
                .runtimeConfiguration(model.getRuntimeConfiguration() == null ? null:
                        model.getRuntimeConfiguration().stream().map(Translator::translate)
                                .collect(Collectors.toList()))
                .schedulerConfiguration(translate(model.getSchedulerConfiguration()))         
                .build();
    }

    private static software.amazon.awssdk.services.emrserverless.model.SchedulerConfiguration translate(SchedulerConfiguration schedulerConfiguration) {
        return schedulerConfiguration == null ? null : software.amazon.awssdk.services.emrserverless.model.SchedulerConfiguration.builder()
            .queueTimeoutMinutes(schedulerConfiguration.getQueueTimeoutMinutes() == null ? null : schedulerConfiguration.getQueueTimeoutMinutes())
            .maxConcurrentRuns(schedulerConfiguration.getMaxConcurrentRuns() == null ? null : schedulerConfiguration.getMaxConcurrentRuns())
            .build();
    }

    private static software.amazon.awssdk.services.emrserverless.model.InteractiveConfiguration translate(InteractiveConfiguration interactiveConfiguration) {
        return interactiveConfiguration == null ? null :
                software.amazon.awssdk.services.emrserverless.model.InteractiveConfiguration.builder()
                        .studioEnabled(interactiveConfiguration.getStudioEnabled() == null ? null : interactiveConfiguration.getStudioEnabled())
                        .livyEndpointEnabled(interactiveConfiguration.getLivyEndpointEnabled() == null ? null : interactiveConfiguration.getLivyEndpointEnabled())
                        .build();
    }

    private static software.amazon.awssdk.services.emrserverless.model.Configuration translate(ConfigurationObject configuration) {
        List<software.amazon.awssdk.services.emrserverless.model.Configuration> configurationList = configuration.getConfigurations() == null ? null :
                configuration.getConfigurations().stream().map(Translator::translate)
                        .collect(Collectors.toList());
        return configuration == null ? null :
                software.amazon.awssdk.services.emrserverless.model.Configuration.builder()
                        .properties(configuration.getProperties() == null ? null : configuration.getProperties())
                        .classification(configuration.getClassification() == null ? null : configuration.getClassification())
                        .configurations(configurationList == null ? null : configurationList.stream().collect(Collectors.toSet()))
                        .build();
    }

    private static software.amazon.awssdk.services.emrserverless.model.MonitoringConfiguration translate(MonitoringConfiguration monitoringConfiguration) {
        return monitoringConfiguration == null
                ? null
                : software.amazon.awssdk.services.emrserverless.model.MonitoringConfiguration.builder()
                .s3MonitoringConfiguration(translate(monitoringConfiguration.getS3MonitoringConfiguration()))
                .managedPersistenceMonitoringConfiguration(translate(monitoringConfiguration.getManagedPersistenceMonitoringConfiguration()))
                .cloudWatchLoggingConfiguration(translate(monitoringConfiguration.getCloudWatchLoggingConfiguration()))
                .build();
    }


    private static software.amazon.awssdk.services.emrserverless.model.S3MonitoringConfiguration translate(S3MonitoringConfiguration s3MonitoringConfiguration) {
        return s3MonitoringConfiguration == null
                ? null
                : software.amazon.awssdk.services.emrserverless.model.S3MonitoringConfiguration.builder()
                .logUri(s3MonitoringConfiguration.getLogUri() == null ? null : s3MonitoringConfiguration.getLogUri())
                .encryptionKeyArn(s3MonitoringConfiguration.getEncryptionKeyArn() == null ? null : s3MonitoringConfiguration.getEncryptionKeyArn())
                .build();
    }

    private static software.amazon.awssdk.services.emrserverless.model.ManagedPersistenceMonitoringConfiguration translate(ManagedPersistenceMonitoringConfiguration managedPersistenceMonitoringConfiguration) {
        return managedPersistenceMonitoringConfiguration == null
                ? null
                : software.amazon.awssdk.services.emrserverless.model.ManagedPersistenceMonitoringConfiguration.builder()
                .enabled(managedPersistenceMonitoringConfiguration.getEnabled() == null ? null : managedPersistenceMonitoringConfiguration.getEnabled())
                .encryptionKeyArn(managedPersistenceMonitoringConfiguration.getEncryptionKeyArn() == null ? null : managedPersistenceMonitoringConfiguration.getEncryptionKeyArn())
                .build();
    }

    private static software.amazon.awssdk.services.emrserverless.model.CloudWatchLoggingConfiguration translate(CloudWatchLoggingConfiguration cloudWatchLoggingConfiguration) {
        return cloudWatchLoggingConfiguration == null
                ? null
                : software.amazon.awssdk.services.emrserverless.model.CloudWatchLoggingConfiguration.builder()
                .enabled(cloudWatchLoggingConfiguration.getEnabled() == null ? null : cloudWatchLoggingConfiguration.getEnabled())
                .encryptionKeyArn(cloudWatchLoggingConfiguration.getEncryptionKeyArn() == null ? null : cloudWatchLoggingConfiguration.getEncryptionKeyArn())
                .logGroupName(cloudWatchLoggingConfiguration.getLogGroupName() == null ? null : cloudWatchLoggingConfiguration.getLogGroupName())
                .logStreamNamePrefix(cloudWatchLoggingConfiguration.getLogStreamNamePrefix() == null ? null : cloudWatchLoggingConfiguration.getLogStreamNamePrefix())
                .logTypes(cloudWatchLoggingConfiguration.getLogTypeMap() == null ? null : translateMap(cloudWatchLoggingConfiguration.getLogTypeMap()))
                .build();
    }

    private static Map<String, List<String>> translateMap(Set<LogTypeMapKeyValuePair> logTypeMap) {
        return logTypeMap == null
                ? null
                : logTypeMap.stream()
                .filter(logType -> logType.getKey() != null && logType.getValue() != null)
                .collect(Collectors.toMap(
                        LogTypeMapKeyValuePair::getKey,
                        logType -> new ArrayList<>(logType.getValue())
                ));
    }

    /**
     * Request to list resources.
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static ListApplicationsRequest translateToListRequest(final String nextToken, final List<ApplicationState> applicationStates) {

        return ListApplicationsRequest.builder()
            .states(applicationStates)
            .nextToken(nextToken)
            .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only).
     *
     * @param awsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final ListApplicationsResponse awsResponse) {
        return streamOfOrEmpty(awsResponse.applications())
            .map(resource -> ResourceModel.builder()
                .applicationId(resource.id())
                .arn(resource.arn())
                .name(resource.name())
                .releaseLabel(resource.releaseLabel())
                .type(resource.type())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Request to add tags to a resource
     *
     * @param arn       resource arn
     * @param addedTags Map of tags to be added
     * @return awsRequest the aws service request to create a resource
     */
    static TagResourceRequest tagResourceRequest(final String arn, final Map<String, String> addedTags
    ) {
        return TagResourceRequest.builder()
            .resourceArn(arn)
            .tags(addedTags)
            .build();
    }

    /**
     * Request to add tags to a resource
     *
     * @param arn         resource arn
     * @param removedTags Map of tags to be removed
     * @return awsRequest the aws service request to create a resource
     */
    static UntagResourceRequest untagResourceRequest(final String arn,
                                                     final Set<String> removedTags) {
        return UntagResourceRequest.builder()
            .resourceArn(arn)
            .tagKeys(removedTags)
            .build();
    }
}
