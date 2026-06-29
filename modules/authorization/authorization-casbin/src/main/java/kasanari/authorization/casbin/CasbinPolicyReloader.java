package kasanari.authorization.casbin;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.management.security.RoleBindingRepository;
import org.jdbi.v3.core.Handle;

import java.util.logging.Level;
import java.util.logging.Logger;

final class CasbinPolicyReloader {
    private static final Logger logger = Logger.getLogger(CasbinPolicyReloader.class.getName());
    private static final long UNLOADED_REVISION = -1L;

    private final TransactionManager<Handle> txManager;
    private final RoleBindingRepository<Handle> roleBindingRepository;
    private final CasbinPolicyHolder policyHolder;
    private volatile long localRevision = UNLOADED_REVISION;

    CasbinPolicyReloader(
            TransactionManager<Handle> txManager,
            RoleBindingRepository<Handle> roleBindingRepository,
            CasbinPolicyHolder policyHolder
    ) {
        this.txManager = txManager;
        this.roleBindingRepository = roleBindingRepository;
        this.policyHolder = policyHolder;
    }

    synchronized void reloadIfChanged() {
        try {
            var dbRevision = txManager.inTransactionR(roleBindingRepository::currentRevision);
            if (localRevision == dbRevision) {
                return;
            }

            var bindings = txManager.inTransactionR(roleBindingRepository::listAll);
            policyHolder.swap(CasbinBindingPolicyExpander.buildEnforcer(bindings));
            localRevision = dbRevision;
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to reload Casbin policies", e);
        }
    }
}
