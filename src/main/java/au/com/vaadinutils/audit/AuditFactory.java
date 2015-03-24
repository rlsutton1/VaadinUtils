package au.com.vaadinutils.audit;

import java.util.concurrent.atomic.AtomicReference;

public enum AuditFactory
{
    SELF(new AuditorLoggingImpl());

    AuditFactory(Auditor auditor)
    {
	this.auditor.set(auditor);
    }

    // Logger logger = LogManager.getLogger();
    AtomicReference<Auditor> auditor = new AtomicReference<Auditor>();

    public static Auditor getAuditor()
    {
	return AuditFactory.SELF.auditor.get();
    }

    public static void setAuditor(Auditor auditor)
    {
	AuditFactory.SELF.auditor.set(auditor);
    }
}
