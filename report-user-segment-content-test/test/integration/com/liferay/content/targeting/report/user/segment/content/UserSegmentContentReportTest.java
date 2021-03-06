/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.content.targeting.report.user.segment.content;

import com.liferay.content.targeting.analytics.service.AnalyticsEventLocalService;
import com.liferay.content.targeting.api.model.Report;
import com.liferay.content.targeting.api.model.ReportsRegistry;
import com.liferay.content.targeting.model.UserSegment;
import com.liferay.content.targeting.report.user.segment.content.service.UserSegmentContentLocalService;
import com.liferay.content.targeting.service.UserSegmentLocalService;
import com.liferay.content.targeting.service.test.util.TestUtil;
import com.liferay.osgi.util.service.ServiceTrackerUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portlet.journal.model.JournalArticle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * @author Eduardo Garcia
 */
@RunWith(Arquillian.class)
public class UserSegmentContentReportTest {

	@Before
	public void setUp() {
		try {
			_bundle.start();
		}
		catch (BundleException e) {
			e.printStackTrace();
		}

		_analyticsEventLocalService = ServiceTrackerUtil.getService(
			AnalyticsEventLocalService.class, _bundle.getBundleContext());
		_userSegmentContentLocalService = ServiceTrackerUtil.getService(
			UserSegmentContentLocalService.class, _bundle.getBundleContext());
		_userSegmentLocalService = ServiceTrackerUtil.getService(
			UserSegmentLocalService.class, _bundle.getBundleContext());
		_reportsRegistry = ServiceTrackerUtil.getService(
			ReportsRegistry.class, _bundle.getBundleContext());
	}

	@Test
	public void testUserSegmentContentReport() throws Exception {
		ServiceContext serviceContext = TestUtil.getServiceContext();

		Map<Locale, String> nameMap = new HashMap<Locale, String>();

		nameMap.put(LocaleUtil.getDefault(), StringUtil.randomString());

		UserSegment userSegment = _userSegmentLocalService.addUserSegment(
			TestUtil.getUserId(), nameMap, null, serviceContext);

		int initialUserSegmentContentCount =
			_userSegmentContentLocalService.getUserSegmentContentsCount(
				userSegment.getUserSegmentId());

		// Obtain report from registry

		Report report = _reportsRegistry.getReport("UserSegmentContentReport");

		// Test update report without analytics

		report.updateReport(userSegment.getUserSegmentId());

		Assert.assertEquals(
			initialUserSegmentContentCount,
			_userSegmentContentLocalService.getUserSegmentContentsCount(
				userSegment.getUserSegmentId()));

		// Add analytics

		_analyticsEventLocalService.addAnalyticsEvent(
			TestUtil.getUserId(), 1, JournalArticle.class.getName(), 2,
			UserSegment.class.getName(),
			new long[]{userSegment.getUserSegmentId()}, null, "view",
			"127.0.0.1", "ES", "User Agent", "http://localhost", null,
			serviceContext);

		// Test update report with analytics

		report.updateReport(userSegment.getUserSegmentId());

		Assert.assertEquals(
			initialUserSegmentContentCount + 1,
			_userSegmentContentLocalService.getUserSegmentContentsCount(
				userSegment.getUserSegmentId()));
	}

	private AnalyticsEventLocalService _analyticsEventLocalService;

	@ArquillianResource
	private Bundle _bundle;

	private ReportsRegistry _reportsRegistry;
	private UserSegmentContentLocalService
		_userSegmentContentLocalService;
	private UserSegmentLocalService _userSegmentLocalService;

}