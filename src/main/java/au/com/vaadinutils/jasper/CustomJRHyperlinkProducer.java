package au.com.vaadinutils.jasper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.engine.export.JRHyperlinkProducer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CustomJRHyperlinkProducer implements JRHyperlinkProducer
{

	Logger logger = LogManager.getLogger();
	
	@Override
	public String getHyperlink(JRPrintHyperlink hyperlink)
	{
		String ref = hyperlink.getHyperlinkReference();
		if (ref == null)
			return ref;
		try
		{

			Map<String, String> query_pairs = new LinkedHashMap<String, String>();
			String query = ref;
			if (query.contains("?"))
			{
				query = query.substring(query.indexOf("?")+1);
			}
			String[] pairs = query.split("&");
			for (String pair : pairs)
			{
				int idx = pair.indexOf("=");
				if (idx >= 0)
				{
					query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
							URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
				}
			}

			Map<String, String> reportDetails = new HashMap<String, String>();
			String reportName = query_pairs.get("ReportName");
			String title = query_pairs.get("ReportTitle");
			reportDetails.put("ReportFileName", reportName);
			reportDetails.put("ReportTitle", title);

			Map<String, String> paramMap = new HashMap<String, String>();
			for (String key : query_pairs.keySet())
			{
				if (key.startsWith("ReportParameter"))
				{
					paramMap.put(key, query_pairs.get(key));
				}
			}

			Gson gson = new Gson();
			String paramMapJson = gson.toJson(paramMap, new TypeToken<Map<String, String>>()
			{
			}.getType());

			String reportDetailsMapJson = gson.toJson(reportDetails, new TypeToken<Map<String, String>>()
			{
			}.getType());
			// + reportDetailsMapJson + "," + paramMapJson + ")";
			return ("javascript:window.parent.au.com.noojee.reportDrillDown(" + reportDetailsMapJson + ","
					+ paramMapJson + ")").replace("\"", "'");

			// window.parent.au.com.noojee.reportDrillDown(
			// {
			// 'reportFileName':
			// 'CallDetailsPerTeamAgentPerHour_CallDetails.jasper',
			// 'reportTitle': 'Call Details Per Team Agent Per Hour'
			// },
			// {
			// 'ReportParameterStartDate'='$P{StartDate}',
			// 'ReportParameterEndDate'='$P{EndDate}',
			// 'ReportParameterExtension'='$F{loginid}',
			// 'ReportParameterTeamId'='$P{TeamId}',
			// 'ReportParameterHour'='$F{Day}.toString()'
			// }
			//
			// );

		}

		catch (UnsupportedEncodingException e)
		{
			logger.error(e,e);
		}
		return "javascript:alert('Test')";
	}

}
