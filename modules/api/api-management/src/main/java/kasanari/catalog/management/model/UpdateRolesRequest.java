package kasanari.catalog.management.model;

import java.util.ArrayList;
import java.util.List;

public class UpdateRolesRequest {
    private List<RoleBinding> bindings = new ArrayList<>();

    public List<RoleBinding> getBindings() {
        return bindings;
    }

    public void setBindings(List<RoleBinding> bindings) {
        this.bindings = bindings;
    }
}
