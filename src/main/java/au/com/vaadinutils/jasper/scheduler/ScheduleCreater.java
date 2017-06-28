package au.com.vaadinutils.jasper.scheduler;

import com.vaadin.addon.jpacontainer.JPAContainer;

import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;

public interface ScheduleCreater
{

	ReportEmailScheduleEntity create();

	void addContainerFilter(JPAContainer<ReportEmailScheduleEntity> container);

}
