package software.amazon.emrserverless.application;

import software.amazon.cloudformation.proxy.StdCallbackContext;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    Integer retryAttempts = 5;
    boolean stabilizationFailed;
    String applicationArn;
    Map<String, String> tagsToAdd = Collections.emptyMap();
    Set<String> tagsToRemove = Collections.emptySet();
}
