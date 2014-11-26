package au.com.vaadinutils.audit;

import java.util.concurrent.atomic.AtomicReference;

public class AuditFactory
{
	// Logger logger = LogManager.getLogger();
	static final AtomicReference<Auditor> auditor = new AtomicReference<Auditor>();
	{
		auditor.set(new AuditorLoggingImpl());
	}

	public static Auditor getAuditor()
	{
		return auditor.get();
	}

	public static void setAuditor(Auditor auditor)
	{
		AuditFactory.auditor.set(auditor);
	}
}
