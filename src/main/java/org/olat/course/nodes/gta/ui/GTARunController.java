/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeOverviewController;
import org.olat.course.nodes.CourseNodeSegmentPrefs;
import org.olat.course.nodes.CourseNodeSegmentPrefs.CourseNodeSegment;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.reminder.ui.CourseNodeReminderRunController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTARunController extends BasicController implements Activateable2 {
	
	private GTAParticipantController runCtrl;
	private AssessmentCourseNodeOverviewController overviewCtrl;
	private GTACoachSelectionController coachCtrl;
	private GTACoachManagementController manageCtrl;
	private CourseNodeReminderRunController remindersCtrl;

	private Link runLink;
	private Link overviewLink;
	private Link coachLink;
	private Link manageLink;
	private Link remindersLink;
	private Link coachAssignmentLink;
	private VelocityContainer mainVC;
	private CourseNodeSegmentPrefs segmentPrefs;
	private SegmentViewComponent segmentView;
	
	private final GTACourseNode gtaNode;
	private final UserCourseEnvironment userCourseEnv;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public GTARunController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.userCourseEnv = userCourseEnv;
		
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Membership membership = gtaManager.getMembership(getIdentity(), entry, gtaNode);
		if((membership.isCoach() && userCourseEnv.isCoach()) || userCourseEnv.isAdmin()) {
			mainVC = createVelocityContainer("run_segments");
			
			segmentPrefs = new CourseNodeSegmentPrefs(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			overviewLink = LinkFactory.createLink("run.overview", mainVC, this);
			overviewLink.setElementCssClass("o_sel_course_gta_overview");
			segmentView.addSegment(overviewLink, true);
			coachLink = LinkFactory.createLink("run.coach.participants", mainVC, this);
			coachLink.setElementCssClass("o_sel_course_gta_coaching");
			segmentView.addSegment(coachLink, false);
			if(isManagementTabAvalaible(config)) {
				manageLink = LinkFactory.createLink("run.manage.coach", mainVC, this);
				manageLink.setElementCssClass("o_sel_course_gta_management");
				segmentView.addSegment(manageLink, false);
			}
			
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Overview"), null);
			overviewCtrl = courseAssessmentService.getCourseNodeOverviewController(ureq, swControl, gtaNode, userCourseEnv, false, true, false);
			listenTo(overviewCtrl);
			
			if (userCourseEnv.isAdmin() && !userCourseEnv.isCourseReadOnly()) {
				swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Reminders"), null);
				remindersCtrl = new CourseNodeReminderRunController(ureq, swControl, entry, gtaNode.getReminderProvider(entry, false));
				listenTo(remindersCtrl);
				if (remindersCtrl.hasDataOrActions()) {
					remindersLink = LinkFactory.createLink("run.reminders", mainVC, this);
					segmentView.addSegment(remindersLink, false);
				}
			}
			
			doOpenPreferredSegment(ureq);
			if(userCourseEnv.isAdmin() && config.getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT)) {
				coachAssignmentWarning();
			}
			mainVC.put("segments", segmentView);
			putInitialPanel(mainVC);
		} else if(membership.isParticipant() && userCourseEnv.isParticipant()) {
			createRun(ureq);
			putInitialPanel(runCtrl.getInitialComponent());
		} else {
			String title = translate("error.not.member.title");
			String message = translate("error.not.member.message");
			Controller msgCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
			listenTo(msgCtrl);
			putInitialPanel(msgCtrl.getInitialComponent());
		}
	}
	
	private void coachAssignmentWarning() {
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if(assessmentService.hasAssessmentEntryWithoutCoachAssignment(courseEntry, gtaNode.getIdent())) {
			coachAssignmentLink = LinkFactory.createLink("coach.assignment", getTranslator(), this);
			coachAssignmentLink.setIconRightCSS("o_icon o_icon_start");
			coachAssignmentLink.setElementCssClass("o_process_assignment");
			mainVC.put("coach.assignment", coachAssignmentLink);
		}
	}
	
	private boolean isManagementTabAvalaible(ModuleConfiguration config) {
		return (userCourseEnv.isAdmin()
				|| (userCourseEnv.isCoach() && config.getBooleanSafe(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, false)))
				&& (config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT) || config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("overview".equalsIgnoreCase(type)) {
			if(overviewLink != null || overviewCtrl != null) {
				doOpenOverview(ureq, true);
				if(segmentView != null) {
					segmentView.select(overviewLink);
				}
			}
		} if("coach".equalsIgnoreCase(type)) {
			if(coachLink != null || coachCtrl != null) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doOpenCoach(ureq, true).activate(ureq, subEntries, entries.get(0).getTransientState());
				if(segmentView != null) {
					segmentView.select(coachLink);
				}
			}
		} else if("management".equalsIgnoreCase(type)) {
			if(manageLink != null) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doManage(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				if(segmentView != null) {
					segmentView.select(manageLink);
				}
			}
		} else if("Reminders".equalsIgnoreCase(type)) {
			if(remindersLink != null) {
				doOpenReminders(ureq, true);
				if(segmentView != null) {
					segmentView.select(remindersLink);
				}
			}
		} else if("identity".equalsIgnoreCase(type)) {
			if(getIdentity().getKey().equals(entries.get(0).getOLATResourceable().getResourceableId())) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doOpenRun(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
				if(segmentView != null) {
					segmentView.select(runLink);
				}
			}
		} else {
			if("CourseNode".equalsIgnoreCase(entries.get(0).getOLATResourceable().getResourceableTypeName())) {
				state = entries.get(0).getTransientState();
				entries = entries.subList(1, entries.size());
			}
			
			if(runCtrl != null && segmentView == null) {
				runCtrl.activate(ureq, entries, state);
			}
			if(coachCtrl != null) {
				coachCtrl.activate(ureq, entries, state);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(runCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(coachAssignmentLink == source) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromResourceType("Assignments");
			doOpenCoach(ureq, true).activate(ureq, entries, null);
		} else if(source == segmentView) {
			if(event instanceof SegmentViewEvent sve) {
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == runLink) {
					doOpenRun(ureq);
				} else if (clickedLink == overviewLink) {
					doOpenOverview(ureq, true);
				} else if (clickedLink == coachLink) {
					doOpenCoach(ureq, true);
				} else if(clickedLink == manageLink) {
					doManage(ureq);
				} else if (clickedLink == remindersLink) {
					doOpenReminders(ureq, true);
				}
			}
		}
	}
	
	private void doOpenPreferredSegment(UserRequest ureq) {
		CourseNodeSegment segment = segmentPrefs.getSegment(ureq);
		if (CourseNodeSegment.overview == segment && overviewLink != null) {
			doOpenOverview(ureq, false);
		} else if (CourseNodeSegment.participants == segment && coachLink != null) {
			doOpenCoach(ureq, false);
		} else if (CourseNodeSegment.reminders == segment && remindersLink != null) {
			doOpenReminders(ureq, false);
		} else {
			doOpenOverview(ureq, false);
		}
	}

	private void setPreferredSegment(UserRequest ureq, CourseNodeSegment segment, boolean saveSegmentPref) {
		if (segmentPrefs != null) {
			segmentPrefs.setSegment(ureq, segment, segmentView, saveSegmentPref);
		}
	}
	
	private Activateable2 doOpenRun(UserRequest ureq) {
		if(runCtrl == null) {
			createRun(ureq);
		}
		addToHistory(ureq, runCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", runCtrl.getInitialComponent());
		}
		return runCtrl;
	}
	
	private void doOpenOverview(UserRequest ureq, boolean saveSegmentPref) {
		overviewCtrl.reload();
		addToHistory(ureq, overviewCtrl);
		mainVC.put("segmentCmp", overviewCtrl.getInitialComponent());
		segmentView.select(overviewLink);
		setPreferredSegment(ureq, CourseNodeSegment.overview, saveSegmentPref);
	}

	private Activateable2 doOpenCoach(UserRequest ureq, boolean saveSegmentPref) {
		if(coachCtrl == null) {
			createCoach(ureq);
		} else {
			coachCtrl.reload(ureq);
		}
		addToHistory(ureq, coachCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", coachCtrl.getInitialComponent());
		}
		segmentView.select(coachLink);
		setPreferredSegment(ureq, CourseNodeSegment.participants, saveSegmentPref);
		return coachCtrl;
	}
	
	private GTACoachManagementController doManage(UserRequest ureq) {
		if(manageCtrl == null) {
			createManage(ureq);
		}
		addToHistory(ureq, manageCtrl);
		if(mainVC != null) {
			mainVC.put("segmentCmp", manageCtrl.getInitialComponent());
		}
		segmentView.select(manageLink);
		return manageCtrl;
	}
	
	private GTAParticipantController createRun(UserRequest ureq) {
		removeAsListenerAndDispose(runCtrl);
		
		runCtrl = new GTAParticipantController(ureq, getWindowControl(), gtaNode, userCourseEnv);
		listenTo(runCtrl);
		return runCtrl;
	}
	
	private GTACoachSelectionController createCoach(UserRequest ureq) {
		removeAsListenerAndDispose(coachCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("coach"), null);
		coachCtrl = new GTACoachSelectionController(ureq, swControl, userCourseEnv, gtaNode);
		listenTo(coachCtrl);
		return coachCtrl;
	}
	
	private GTACoachManagementController createManage(UserRequest ureq) {
		removeAsListenerAndDispose(manageCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("management"), null);
		manageCtrl = new GTACoachManagementController(ureq, swControl, userCourseEnv, gtaNode);
		listenTo(manageCtrl);
		return manageCtrl;
	}
	
	private void doOpenReminders(UserRequest ureq, boolean saveSegmentPref) {
		if (remindersLink != null) {
			remindersCtrl.reload(ureq);
			mainVC.put("segmentCmp", remindersCtrl.getInitialComponent());
			segmentView.select(remindersLink);
			setPreferredSegment(ureq, CourseNodeSegment.reminders, saveSegmentPref);
		}
	}
}