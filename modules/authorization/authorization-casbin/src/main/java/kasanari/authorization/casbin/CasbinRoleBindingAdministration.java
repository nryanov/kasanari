package kasanari.authorization.casbin;

import kasanari.authorization.spi.RoleBinding;
import kasanari.authorization.spi.RoleBindingAdministration;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.management.security.RoleBindingRepository;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.casbin.jcasbin.main.Enforcer;
import org.jdbi.v3.core.Handle;

import java.util.List;

final class CasbinRoleBindingAdministration implements RoleBindingAdministration {
    private final TransactionManager<Handle> txManager;
    private final RoleBindingRepository<Handle> roleBindingRepository;
    private final Enforcer enforcer;

    CasbinRoleBindingAdministration(
            TransactionManager<Handle> txManager,
            RoleBindingRepository<Handle> roleBindingRepository,
            Enforcer enforcer
    ) {
        this.txManager = txManager;
        this.roleBindingRepository = roleBindingRepository;
        this.enforcer = enforcer;
    }

    @Override
    public List<RoleBinding> list(String subject, String resource) {
        return txManager.inTransactionR(tx -> roleBindingRepository.list(tx, subject, resource)).stream()
                .map(CasbinRoleBindingAdministration::toSpi)
                .toList();
    }

    @Override
    public void add(List<RoleBinding> bindings) {
        txManager.inTransaction(tx -> roleBindingRepository.add(tx, toStored(bindings)));
    }

    @Override
    public void delete(List<RoleBinding> bindings) {
        txManager.inTransaction(tx -> roleBindingRepository.delete(tx, toStored(bindings)));
    }

    @Override
    public void reloadPolicies() {
        var storedBindings = txManager.inTransactionR(roleBindingRepository::listAll);
        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, storedBindings);
    }

    private static RoleBinding toSpi(StoredRoleBinding binding) {
        return new RoleBinding(binding.subject(), binding.resource(), binding.role());
    }

    private static List<StoredRoleBinding> toStored(List<RoleBinding> bindings) {
        return bindings.stream()
                .map(binding -> new StoredRoleBinding(binding.subject(), binding.resource(), binding.role()))
                .toList();
    }
}
