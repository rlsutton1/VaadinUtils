package au.com.vaadinutils.jasper.scheduler;

import au.com.vaadinutils.jasper.scheduler.entities.ReportEmailScheduleEntity;

import com.vaadin.addon.jpacontainer.JPAContainer;

public interface ScheduleCreater
{

	ReportEmailScheduleEntity create();

	void addContainerFilter(JPAContainer<ReportEmailScheduleEntity> container);

}
