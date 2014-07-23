package au.com.vaadinutils.jasper.scheduler;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import au.com.vaadinutils.dao.EntityManagerProvider;
import au.com.vaadinutils.dao.JpaBaseDao;
import au.com.vaadinutils.jasper.JasperEmailSettings;
import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;

public enum JasperReportScheudulerService implements ReportEmailScheduleProvider
{
	// Logger logger = LogManager.getLogger();
	SELF;

	private Scheduler scheduler;
	

	public void start(JasperEmailSettings settings, DBmanager dbManager)
	{
		ReportEmailRunner runner = new ReportEmailRunnerImpl();

		scheduler = new Scheduler(this, runner, settings,dbManager);
	}
	
	public void stop()
	{
		scheduler.stop();
	}
	
	public void reschedule()
	{
		scheduler.reschedule();
	}

	/**
	 * return all the scheduled reports
	 */
	@Override
	public List<ReportEmailSchedule> getSchedules()
	{
		JpaBaseDao<ReportEmailScheduleEntity, Long> dao = JpaBaseDao.getGenericDao(ReportEmailScheduleEntity.class);
		List<ReportEmailSchedule> schedules = new LinkedList<ReportEmailSchedule>();
		schedules.addAll(dao.findAll());
		return schedules;
	}



//	@Override
//	public void commitDbTransaction()
//	{
//		EntityManager em = EntityManagerProvider.getEntityManager();
//		em.getTransaction().commit();
//		EntityManagerProvider.setCurrentEntityManager(null);
//		em.close();
//	}
//
//	@Override
//	public void beginDbTransaction()
//	{
//		EntityManager em = EntityManagerProvider.createEntityManager();
//		EntityManagerProvider.setCurrentEntityManager(em);
//		em.getTransaction().begin();
//	}

	@Override
	public void delete(ReportEmailSchedule schedule)
	{
		EntityManager em = EntityManagerProvider.getEntityManager();
		em.remove(schedule);
		
	}

}
