package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.OpenBadgesManager.BadgeAssertionWithSize;
import org.olat.modules.openbadges.ui.BadgeAssertionPublicController;
import org.olat.modules.openbadges.ui.BadgeImageComponent;
import org.olat.modules.openbadges.ui.BadgeToolTableModel;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityBadgesAssertionsController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private FlexiTableElement tableEl;
	private IdentityBadgesAssertionsTableModel tableModel;
	
	private int count = 0;
	private final String mediaUrl;
	private Identity assessedIdentity;
	private RepositoryEntry courseEntry;
	
	private CloseableModalController cmc;
	private BadgeAssertionPublicController badgeAssertionPublicController; 
	
	@Autowired
	private OpenBadgesManager openBadgesManager;
	
	public IdentityBadgesAssertionsController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, Identity assessedIdentity) {
		super(ureq, wControl, "badge_overview");
		this.courseEntry = courseEntry;
		this.assessedIdentity = assessedIdentity;
		mediaUrl = registerMapper(ureq, new BadgeImageMapper());
		
		initForm(ureq);
		loadModel();
	}
	
	public boolean hasBadgesAssertions() {
		return tableModel != null && tableModel.getRowCount() > 0;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BadgeToolTableModel.AssertionCols.name, "select"));

		tableModel = new IdentityBadgesAssertionsTableModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24,
				false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setExportEnabled(false);
		tableEl.setNumOfRowsEnabled(false);
		VelocityContainer rowVC = createVelocityContainer("badge_row_1");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(3);
		if (rowObject instanceof IdentityBadgeAssertionRow badgeRow) {
			cmps.add(badgeRow.getBadgeImage());
			if(badgeRow.getDownloadLink() != null) {
				cmps.add(badgeRow.getDownloadLink().getComponent());
			}
			if(badgeRow.getSelectLink() != null) {
				cmps.add(badgeRow.getSelectLink().getComponent());
			}
		}
		return cmps;
	}

	private void loadModel() {
		List<BadgeAssertionWithSize> badges = openBadgesManager.getBadgeAssertionsWithSizes(assessedIdentity, courseEntry, true);
		List<IdentityBadgeAssertionRow> badgeToolRows = badges.stream()
				.map(ba -> {
					IdentityBadgeAssertionRow row = new IdentityBadgeAssertionRow(ba);
					forgeRow(row, ba);
					return row;
				}).toList();
		tableModel.setObjects(badgeToolRows);
		tableEl.reset(true, true, true);
		flc.contextPut("rowCount", Integer.valueOf(tableModel.getRowCount()));
	}
	
	private void forgeRow(IdentityBadgeAssertionRow row, OpenBadgesManager.BadgeAssertionWithSize badgeAssertionWithSize) {
		BadgeAssertion badgeAssertion = badgeAssertionWithSize.badgeAssertion();
		String imageUrl = mediaUrl + "/" + badgeAssertion.getBakedImage();
		BadgeImageComponent badgeImage = new BadgeImageComponent("badgeImage", imageUrl, BadgeImageComponent.Size.smallCardSize);
		row.setBadgeImage(badgeImage);
		row.setIssuedOn(Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn()));
		
		FormLink downloadLink = uifactory.addFormLink("download." + (++count), "download", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		downloadLink.setElementCssClass("o_sel_badge_download");
		downloadLink.setIconLeftCSS("o_icon o_icon-lg o_icon_download");
		downloadLink.setTooltip(translate("download.badge"));
		downloadLink.setNewWindow(true, false, false);
		downloadLink.setUserObject(row);
		row.setDownloadLink(downloadLink);
		
		FormLink selectLink = uifactory.addFormLink("select." + (++count), "select", row.getName(), null, flc, Link.LINK | Link.NONTRANSLATED);
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}
	
	@Override
	protected void event(UserRequest ureq,Controller source, Event event) {
		if(badgeAssertionPublicController == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(badgeAssertionPublicController);
		removeAsListenerAndDispose(cmc);
		badgeAssertionPublicController = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link && link.getUserObject() instanceof IdentityBadgeAssertionRow badgeRow) {
			String cmd = link.getCmd();
			if("download".equals(cmd)) {
				doDownLoad(ureq, badgeRow);
			} else if("select".equals(cmd)) {
				doSelect(ureq, badgeRow);
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doDownLoad(UserRequest ureq, IdentityBadgeAssertionRow badgeRow) {
		String relPath = badgeRow.getBadgeAssertion().getBakedImage();
		VFSLeaf leaf = openBadgesManager.getBadgeAssertionVfsLeaf(relPath);
		VFSMediaResource badgeAssertion = new VFSMediaResource(leaf);
		badgeAssertion.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(badgeAssertion);
	}

	private void doSelect(UserRequest ureq, IdentityBadgeAssertionRow badgeRow) {
		BadgeAssertion badgeAssertion = badgeRow.getBadgeAssertion();
		badgeAssertionPublicController = new BadgeAssertionPublicController(ureq, getWindowControl(), badgeAssertion.getUuid());
		listenTo(badgeAssertionPublicController);

		String title = translate("badge.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				badgeAssertionPublicController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private class BadgeImageMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf templateLeaf = openBadgesManager.getBadgeAssertionVfsLeaf(relPath);
			if (templateLeaf != null) {
				return new VFSMediaResource(templateLeaf);
			}
			return new NotFoundMediaResource();
		}
	}
}
