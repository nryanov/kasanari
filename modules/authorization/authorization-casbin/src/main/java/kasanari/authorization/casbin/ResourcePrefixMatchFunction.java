package kasanari.authorization.casbin;

import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import org.casbin.jcasbin.util.function.CustomFunction;

import java.util.Map;

final class ResourcePrefixMatchFunction extends CustomFunction {
    static final String NAME = "resourcePrefixMatch";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject requestPathArg, AviatorObject bindingPathArg) {
        var requestPath = requestPathArg.stringValue(env);
        var bindingPath = bindingPathArg.stringValue(env);
        return AviatorBoolean.valueOf(matches(requestPath, bindingPath));
    }

    private static boolean matches(String requestPath, String bindingPath) {
        if (requestPath == null || bindingPath == null) {
            return false;
        }
        if (requestPath.equals(bindingPath)) {
            return true;
        }
        return requestPath.startsWith(bindingPath + "/");
    }
}
