package kasanari.catalog.management.dto;

import java.util.ArrayList;
import java.util.List;

public class AddRolesRequestDto {
    private List<RoleBindingDto> bindings = new ArrayList<>();

    public List<RoleBindingDto> getBindings() {
        return bindings;
    }

    public void setBindings(List<RoleBindingDto> bindings) {
        this.bindings = bindings;
    }
}
