package software.amazon.emrserverless.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.emrserverless.model.Application;
import software.amazon.awssdk.services.emrserverless.model.ApplicationState;
import software.amazon.awssdk.services.emrserverless.model.ConflictException;
import software.amazon.awssdk.services.emrserverless.model.CreateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.DeleteApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.EmrServerlessException;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.GetApplicationResponse;
import software.amazon.awssdk.services.emrserverless.model.InternalServerException;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsRequest;
import software.amazon.awssdk.services.emrserverless.model.ListApplicationsResponse;
import software.amazon.awssdk.services.emrserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.emrserverless.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.emrserverless.model.TagResourceRequest;
import software.amazon.awssdk.services.emrserverless.model.UntagResourceRequest;
import software.amazon.awssdk.services.emrserverless.model.UpdateApplicationRequest;
import software.amazon.awssdk.services.emrserverless.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;

import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
            .collect(Collectors.toMap(entry -> entry.getKey(),
                entry -> translate(entry.getValue())));
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
                .workerCount(Optional.ofNullable(config.getWorkerCount())
                    .orElse(null))
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
                .build())
            .orElse(null);
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
                .workerCount(Optional.ofNullable(config.workerCount())
                    .orElse(null))
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
     * @param exception Emr-serverless exception
     * @param operation Operation for which the exception is thrown.
     * @param resourceId Resource identifier for which the operation is invoked.
     * @return Translated cfn handler exception
     */
    static BaseHandlerException translate(final EmrServerlessException exception, final String operation, final String resourceId, final CallbackContext callbackContext) {
        return Optional.ofNullable(exception)
            .map(e -> {
                if (e instanceof ValidationException) {
                    return new CfnInvalidRequestException(e.getMessage(), e);
                } else if (e instanceof ResourceNotFoundException) {
                    return StringUtils.isEmpty(resourceId)
                        ? new CfnNotFoundException(e)
                        : new CfnNotFoundException(ResourceModel.TYPE_NAME, resourceId);
                } else if (e instanceof InternalServerException && callbackContext.isStabilizationFailed()){
                    return new CfnNotStabilizedException(ResourceModel.TYPE_NAME, resourceId, e);
                } else if (e instanceof InternalServerException){
                    return new CfnServiceInternalErrorException(operation, e);
                } else if (e instanceof ServiceQuotaExceededException) {
                    return new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.getMessage(), e);
                } else if (e instanceof ConflictException) {
                    return StringUtils.isEmpty(resourceId)
                        ? new CfnResourceConflictException(e)
                        : new CfnResourceConflictException(ResourceModel.TYPE_NAME, resourceId, e.getMessage(), e);
                } else {
                    return new CfnGeneralServiceException(operation, e);
                }
            })
            .orElse(null);
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
     * @param model resource model
     * @param request request
     * @return UpdateApplicationRequest the aws service request to create a resource
     */
    static UpdateApplicationRequest translateToUpdateRequest(final ResourceModel model, final ResourceHandlerRequest<ResourceModel> request) {
        return UpdateApplicationRequest.builder()
            .applicationId(model.getApplicationId())
            .clientToken(request.getClientRequestToken())
            .autoStartConfiguration(translate(model.getAutoStartConfiguration()))
            .autoStopConfiguration(translate(model.getAutoStopConfiguration()))
            .initialCapacity(translate(model.getInitialCapacity()))
            .maximumCapacity(translate(model.getMaximumCapacity()))
            .networkConfiguration(translate(model.getNetworkConfiguration()))
            .build();
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
     * @param arn resource arn
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
     * @param arn resource arn
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
