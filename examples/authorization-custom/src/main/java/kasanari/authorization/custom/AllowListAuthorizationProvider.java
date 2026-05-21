package kasanari.authorization.custom;

import kasanari.authorization.spi.AuthorizationProvider;
import kasanari.authorization.spi.AuthorizationProviderContext;
import kasanari.authorization.spi.AuthorizationRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class AllowListAuthorizationProvider implements AuthorizationProvider {
    private final Set<String> allowedSubjects = new HashSet<>();

    @Override
    public String type() {
        return "allow-list";
    }

    @Override
    public void initialize(AuthorizationProviderContext context) {
        context.getOptional("allowed-subjects")
                .ifPresent(value -> Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(subject -> !subject.isEmpty())
                        .map(subject -> subject.toLowerCase(Locale.ROOT))
                        .forEach(allowedSubjects::add));
    }

    @Override
    public boolean isAuthorized(AuthorizationRequest request) {
        return allowedSubjects.contains(request.subject().toLowerCase(Locale.ROOT));
    }
}
