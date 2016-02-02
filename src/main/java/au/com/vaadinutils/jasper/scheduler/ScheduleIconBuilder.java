package au.com.vaadinutils.jasper.scheduler;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

public class ScheduleIconBuilder
{

	Logger logger = LogManager.getLogger();

	/**
	 * image is rendered at twice the requested size to guarantee good quality
	 *
	 * @param width
	 * @param title
	 * @param imagePath
	 *            path to where the njIcon.png file can be found, this path is
	 *            also where the title image will be saved.
	 * @param fileName
	 *            - the filename the title image will be saved as
	 */
	public void buildLogo(int number, File imagePath, String templateFileName, String targetFileName)
	{
		try
		{

			Preconditions.checkArgument(imagePath.exists(),
					"Image path " + imagePath.getAbsolutePath() + "/" + targetFileName + " doesn't exist");
			File targetFile = new File(imagePath, targetFileName);

			if (targetFile.exists())
			{
				return;
			}

			// create transulcent graphics object
			BufferedImage bImg = ImageIO.read(new File(imagePath, templateFileName));
			Graphics2D graphics = bImg.createGraphics();

			// create shaded background rectangle

			// graphics.setColor(new Color(255, 0, 0));
			// graphics.fillRoundRect(width/2, height/2, width/2, height/2,4,4);

			// set up font for "Noojeee Telephony Solutions"
			graphics.setColor(new Color(0, 0, 0));
			Font font = new Font("Sans Serif", Font.TRUETYPE_FONT + Font.BOLD, 16);

			FontMetrics metrics = graphics.getFontMetrics(font);
			graphics.setFont(font);

			// position "Noojee Telephony Solutions"
			String nts = "" + number;

			graphics.drawString(nts, 2, (metrics.getHeight() / 2) + 6);

			// save the image

			if (ImageIO.write(bImg, "png", targetFile))
			{
				logger.info("Saved icon image to " + targetFile.getAbsolutePath());
			}

		}
		catch (IOException e)
		{
			logger.error(e, e);
		}

	}
}
