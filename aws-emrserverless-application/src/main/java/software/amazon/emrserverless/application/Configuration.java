package software.amazon.emrserverless.application;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-emrserverless-application.json");
    }

    public Map<String, String> resourceDefinedTags(final ResourceModel model) {
        return Optional.ofNullable(model.getTags())
                .orElse(Collections.emptySet())
                .stream()
                .collect(Collectors.toMap(Tag::getKey, Tag::getValue, (v1, v2) -> v2));
    }
}
