package au.com.vaadinutils.jasper.scheduler.entities;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tblreportemailrecipient")
public class ReportEmailRecipient implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long iID;

	String emailAddress;

	@Enumerated(EnumType.STRING)
	ReportEmailRecipientVisibility visibility;

	public String getEmail()
	{
		return emailAddress;
	}

	public void setEmail(String email)
	{
		emailAddress = email;

	}

	public void setVisibility(ReportEmailRecipientVisibility value)
	{
		visibility = value;

	}

	public ReportEmailRecipientVisibility getVisibility()
	{
		return visibility;
	}

}
