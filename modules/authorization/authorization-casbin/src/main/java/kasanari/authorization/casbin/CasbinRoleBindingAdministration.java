package kasanari.authorization.casbin;

import kasanari.authorization.spi.RoleBinding;
import kasanari.authorization.spi.RoleBindingAdministration;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.management.security.RoleBindingRepository;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.jdbi.v3.core.Handle;

import java.util.List;

final class CasbinRoleBindingAdministration implements RoleBindingAdministration {
    private final TransactionManager<Handle> txManager;
    private final RoleBindingRepository<Handle> roleBindingRepository;
    private final CasbinPolicyEngine policyEngine;

    CasbinRoleBindingAdministration(
            TransactionManager<Handle> txManager,
            RoleBindingRepository<Handle> roleBindingRepository,
            CasbinPolicyEngine policyEngine
    ) {
        this.txManager = txManager;
        this.roleBindingRepository = roleBindingRepository;
        this.policyEngine = policyEngine;
    }

    @Override
    public List<RoleBinding> list(String subject, String resource) {
        return txManager.inTransactionR(tx -> roleBindingRepository.list(tx, subject, resource)).stream()
                .map(this::toSpi)
                .toList();
    }

    @Override
    public void add(List<RoleBinding> bindings) {
        var stored = toStored(bindings);
        if (stored.isEmpty()) {
            return;
        }
        txManager.inTransaction(tx -> {
            roleBindingRepository.add(tx, stored);
            roleBindingRepository.bumpRevision(tx);
        });
    }

    @Override
    public void delete(List<RoleBinding> bindings) {
        var stored = toStored(bindings);
        if (stored.isEmpty()) {
            return;
        }
        txManager.inTransaction(tx -> {
            roleBindingRepository.delete(tx, stored);
            roleBindingRepository.bumpRevision(tx);
        });
    }

    @Override
    public void reloadPolicies() {
        policyEngine.reloadIfChanged();
    }

    private RoleBinding toSpi(StoredRoleBinding binding) {
        return new RoleBinding(binding.subject(), binding.resource(), binding.role(), binding.effect());
    }

    private List<StoredRoleBinding> toStored(List<RoleBinding> bindings) {
        return bindings.stream()
                .map(binding -> new StoredRoleBinding(
                        binding.subject(),
                        binding.resource(),
                        binding.role(),
                        binding.effect()
                ))
                .toList();
    }
}
