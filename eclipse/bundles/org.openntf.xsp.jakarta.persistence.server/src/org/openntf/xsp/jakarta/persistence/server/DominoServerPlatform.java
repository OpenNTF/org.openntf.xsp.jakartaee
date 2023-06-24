package org.openntf.xsp.jakarta.persistence.server;

import org.eclipse.persistence.platform.server.ServerPlatformBase;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.transaction.JTATransactionController;

public class DominoServerPlatform extends ServerPlatformBase {

	public DominoServerPlatform(DatabaseSession newDatabaseSession) {
        super(newDatabaseSession);
        this.disableRuntimeServices();
        
        System.out.println(">>> DominoServerPlatform init");
    }

    /**
     * INTERNAL: getExternalTransactionControllerClass(): Answer the class of external transaction controller to use
     * in the DatabaseSession
     * This is defined by the user via the 904 sessions.xml.
     *
     * @return Class externalTransactionControllerClass
     *
     * @see org.eclipse.persistence.transaction.JTATransactionController
     * @see #isJTAEnabled()
     * @see #disableJTA()
     * @see #initializeExternalTransactionController()
     */
    @Override
    public Class getExternalTransactionControllerClass() {
        return JTATransactionController.class;
    }

    /**
     * INTERNAL: externalTransactionControllerNotNullWarning():
       * When the external transaction controller is being initialized, we warn the developer
       * if they have already defined the external transaction controller in some way other
       * than subclassing ServerPlatformBase.
       *
       * This warning is omitted in 9.0.4.
     *
       * @see ServerPlatformBase
     *
     */
    @Override
    protected void externalTransactionControllerNotNullWarning() {
        //do nothing, because it would be really annoying to show a warning here
    }

}
