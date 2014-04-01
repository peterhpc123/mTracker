package com.ebay.build.service;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.ebay.build.alerts.devx.DevxScheduler;
import com.ebay.build.alerts.pfdash.PfDashScheduler;
import com.ebay.build.service.config.BuildServiceConfig;
import com.ebay.build.tracking.TrackingScheduler;
import com.ebay.build.udc.UDCSheduler;

public class BuildServiceScheduler implements ServletContextListener {
	public static File contextPath;
	
	public static void setContextPath(String contextPath) {
		File outputPath = new File(contextPath);
		
		if (!outputPath.exists()) {
			outputPath.mkdirs();
		}
		
		BuildServiceScheduler.contextPath = outputPath.getAbsoluteFile();
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("BuildServiceScheduler exit.");
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		String path = contextEvent.getServletContext().getRealPath("generated-source");
		setContextPath(path);
		
		System.out.println("BuildServiceScheduler init start. output path: " + path);
		
		if (isSchedulerEnabled()) {
//			HealthTrackScheduler healthTrackScheduler = new HealthTrackScheduler();
//			ReliabilityEmailScheduler reliabilityScheduler = new ReliabilityEmailScheduler();
			PfDashScheduler pfDashScheduler = new PfDashScheduler();
			DevxScheduler devxScheduler = new DevxScheduler();

			try {
				//healthTrackScheduler.run();
				//reliabilityScheduler.run();
				pfDashScheduler.run();
				devxScheduler.run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Scheduler is disabled on this server.");
			
			TrackingScheduler tScheduler = new TrackingScheduler();
            // TODO:
            //      we enable this on the standby server,
            //      this is for test purpose, should be moved to the if block
            try {
                tScheduler.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		
		//runs in all machines.
		UDCSheduler udcSheduler = new UDCSheduler();
		try {
			udcSheduler.run();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}

	public boolean isSchedulerEnabled() {
		String serverHostName = getHostName();
		if (serverHostName == null) {
			return false;
		} 
		try {
			String siteService = new BuildServiceConfig().get("com.ebay.build.reliability.email.scheduler").getSite();
			if (serverHostName.equals(siteService)) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("Build Service Scheduler can not access Raptor Build Service due to " + e.getMessage());
		}
		return false;
	}
	
	public static String getHostName() {
		InetAddress netAddress = null;
		try {
			netAddress = InetAddress.getLocalHost();
			if (null == netAddress) {
				return null;
			}			
			String name = netAddress.getCanonicalHostName();
			return name;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
				
		return null;
	}
	
}
