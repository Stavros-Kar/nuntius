/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.folder.ui.FolderDataModel.FolderCols;
import org.olat.core.commons.services.folder.ui.component.QuotaBar;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseRenderer;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataContainer;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.vfs.ui.version.RevisionListController;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.ui.WebDAVController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.mail.ui.SendDocumentsByEMailController;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.project.ui.ProjConfirmationController;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {

	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_FROM_ME = "FromMe";
	private static final String TAB_ID_FOR_ME = "ForMe";
	private static final String FILTER_TYPE = "filter.type";
	private static final String FILTER_INITIALIZED_BY = "filter.initialized.by";
	private static final String FILTER_MODIFIED_DATE = "filter.modified.date";
	private static final String FILTER_TITLE = "filter.title";
	private static final String FILTER_STATUS = "filter.status";
	private static final String FILTER_LICENSE = "filter.license";
	private static final String CMD_FOLDER = "folder";
	private static final String CMD_PATH = "path";
	private static final String CMD_DOWNLOAD = "download";
	private static final String CMD_COPY = "copy";
	private static final String CMD_MOVE = "move";
	private static final String CMD_METADATA = "metadata";
	private static final String CMD_VERSION = "version";
	private static final String CMD_ZIP = "zip";
	private static final String CMD_UNZIP = "unzip";
	private static final String CMD_DELETE = "delete";
	
	private FormLink viewFolderLink;
	private FormLink viewFileLink;
	private FormLink viewSearchLink;
	private TextElement quickSearchEl;
	private FormLink quickSearchButton;
	private FormLink uploadLink;
	private DropdownItem createDropdown;
	private FormLink createDocumentLink;
	private FormLink createFolderLink;
	private SpacerItem recordSpacer;
	private FormLink recordVideoLink;
	private FormLink recordAudioLink;
	private DropdownItem cmdDropdown;
	private FormLink webdavLink;
	private FormLink quotaEditLink;
	private FormLink bulkDownloadButton;
	private FormLink bulkMoveButton;
	private FormLink bulkCopyButton;
	private FormLink bulkZipButton;
	private FormLink bulkEmailButton;
	private TooledStackedPanel folderBreadcrumb;
	private QuotaBar quotaBar;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabFromMe;
	private FlexiFiltersTab tabForMe;
	private FolderDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CloseableModalController cmc;
	private UploadController uploadCtrl;
	private CreateDocumentController createDocumentCtrl;
	private CreateFolderController createFolderCtrl;
	private RecordAVController recordAVController;
	private WebDAVController webdavCtrl;
	private Controller quotaEditCtrl;
	private FolderSelectionController copySelectFolderCtrl;
	private Controller metadataCtrl;
	private RevisionListController revisonsCtrl;
	private ZipConfirmationController zipConfirmationCtrl;
	private ProjConfirmationController deleteSoftlyConfirmationCtrl;
	private SendDocumentsByEMailController emailCtrl;
	
	private final VFSContainer rootContainer;
	private VFSContainer currentContainer;
	private VFSItemFilter vfsFilter = new VFSSystemItemFilter();
	private final FolderControllerConfig config;
	private final boolean licensesEnabled;
	private final boolean webdavEnabled;
	private boolean versionsEnabled;
	private final Formatter formatter;
	private FolderView folderView;
	private int counter = 0;
	
	@Autowired
	private VFSVersionModule vfsVersionModule;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private WebDAVModule webDAVModule;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler licenseHandler;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AVModule avModule;

	public FolderController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, FolderControllerConfig config) {
		super(ureq, wControl, "folder");
		setTranslator(Util.createPackageTranslator(ProjectUIFactory.class, getLocale(), getTranslator()));
		this.rootContainer = rootContainer;
		this.config = config;
		this.licensesEnabled = licenseModule.isEnabled(licenseHandler);
		this.webdavEnabled = config.isDisplayWebDAVLink() && webDAVModule.isEnabled() && webDAVModule.isLinkEnabled()
				&& ureq.getUserSession().getRoles().isGuestOnly();
		this.formatter = Formatter.getInstance(getLocale());
		
		setCurrentContainer(rootContainer);
		reloadVersionsEnabled();
		
		VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(rootContainer);
		if (secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			String data = rootContainer.getRelPath();
			if (subsContext != null && data != null) {
				String businessPath = wControl.getBusinessControl().getAsString();
				PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(FolderModule.class), data, businessPath);
				ContextualSubscriptionController subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
				listenTo(subscriptionCtrl);
				flc.put("subscription", subscriptionCtrl.getInitialComponent());
			}
		}
		
		initForm(ureq);
		doOpenView(ureq, FolderView.folder);
		updateCommandUI(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		folderBreadcrumb = new TooledStackedPanel("folderBreadcrumb", getTranslator(), this);
		formLayout.add(new ComponentWrapperElement(folderBreadcrumb));
		folderBreadcrumb.setToolbarEnabled(false);
		folderBreadcrumb.getBackLink().setVisible(false);
		folderBreadcrumb.pushController(rootContainer.getName(), null, "/");
		
		viewFolderLink = uifactory.addFormLink("view.folder", formLayout, Link.BUTTON);
		viewFolderLink.setIconLeftCSS("o_icon o_icon-lg o_filetype_folder");
		viewFolderLink.setTitle("view.folder.title");
		
		viewFileLink = uifactory.addFormLink("view.file", formLayout, Link.BUTTON);
		viewFileLink.setIconLeftCSS("o_icon o_icon-lg o_filetype_file");
		viewFileLink.setTitle("view.file.title");
		
		viewSearchLink = uifactory.addFormLink("view.search", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		viewSearchLink.setIconLeftCSS("o_icon o_icon-lg o_icon_search");
		viewSearchLink.setElementCssClass("o_folder_view_search");
		viewSearchLink.setTitle(translate("view.search.title"));
		
		quickSearchEl = uifactory.addTextElement("quicksearch", null, 32, "", formLayout);
		quickSearchEl.setPlaceholderKey("enter.search.term", null);
		quickSearchEl.setDomReplacementWrapperRequired(false);
		
		quickSearchButton = uifactory.addFormLink("quickSearchButton", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		quickSearchButton.setIconLeftCSS("o_icon o_icon_search");
		quickSearchButton.setDomReplacementWrapperRequired(false);
		
		uploadLink = uifactory.addFormLink("add", formLayout, Link.BUTTON);
		uploadLink.setIconLeftCSS("o_icon o_icon_upload");
		
		createDropdown = uifactory.addDropdownMenu("create.dropdown", null, null, formLayout, getTranslator());
		createDropdown.setOrientation(DropdownOrientation.right);
		
		createDocumentLink = uifactory.addFormLink("document.create", formLayout, Link.LINK);
		createDocumentLink.setIconLeftCSS("o_icon o_icon_add");
		createDropdown.addElement(createDocumentLink);
		
		createFolderLink = uifactory.addFormLink("folder.create", formLayout, Link.LINK);
		createFolderLink.setIconLeftCSS("o_icon o_icon_new_folder");
		createDropdown.addElement(createFolderLink);
		
		recordSpacer = new SpacerItem("recordSpace");
		createDropdown.addElement(recordSpacer);
		
		recordVideoLink = uifactory.addFormLink("record.video", formLayout, Link.LINK);
		recordVideoLink.setIconLeftCSS("o_icon o_icon-fw o_icon_video_record");
		createDropdown.addElement(recordVideoLink);
		
		recordAudioLink = uifactory.addFormLink("record.audio", formLayout, Link.LINK);
		recordAudioLink.setIconLeftCSS("o_icon o_icon-fw o_icon_audio_record");
		createDropdown.addElement(recordAudioLink);
		
		cmdDropdown = uifactory.addDropdownMenu("cmds", null, null, flc, getTranslator());
		cmdDropdown.setCarretIconCSS("o_icon o_icon_commands");
		cmdDropdown.setOrientation(DropdownOrientation.right);
		
		webdavLink = uifactory.addFormLink("webdav", formLayout, Link.LINK);
		webdavLink.setIconLeftCSS("o_icon o_icon-fw o_icon_external_link");
		cmdDropdown.addElement(webdavLink);
		
		quotaEditLink = uifactory.addFormLink("quota.edit", formLayout, Link.LINK);
		quotaEditLink.setIconLeftCSS("o_icon o_icon-fw o_icon_quota");
		cmdDropdown.addElement(quotaEditLink);
		
		quotaBar = new QuotaBar("quota", null, getLocale());
		formLayout.add(new ComponentWrapperElement(quotaBar));
		updateQuotaBarUI(ureq);
	}

	private void updateCommandUI(UserRequest ureq) {
		boolean canDecendants = VFSConstants.YES == currentContainer.canDescendants();
		viewFileLink.setVisible(canDecendants);
		
		boolean canEditCurrentContainer = canEdit(currentContainer);
		uploadLink.setVisible(canEditCurrentContainer);
		createDropdown.setVisible(canEditCurrentContainer);
		createDocumentLink.setVisible(canEditCurrentContainer);
		createFolderLink.setVisible(canEditCurrentContainer);
		recordSpacer.setVisible(canEditCurrentContainer && avModule.isRecordingEnabled());
		recordVideoLink.setVisible(canEditCurrentContainer && avModule.isVideoRecordingEnabled());
		recordAudioLink.setVisible(canEditCurrentContainer && avModule.isAudioRecordingEnabled());
		
		webdavLink.setVisible(webdavEnabled);
		quotaEditLink.setVisible(canEditQuota(ureq));
		cmdDropdown.setVisible(webdavLink.isVisible() || quotaEditLink.isVisible());
		
		bulkMoveButton.setVisible(canEditCurrentContainer);
		bulkCopyButton.setVisible(canEditCurrentContainer);
		bulkZipButton.setVisible(canEditCurrentContainer);
	}
	
	private boolean canEditQuota(UserRequest ureq) {
		if (quotaManager.hasMinimalRolesToEditquota(ureq.getUserSession().getRoles())) {
			Quota quota = VFSManager.isTopLevelQuotaContainer(currentContainer);
			if(quota != null) {
				return quotaManager.hasQuotaEditRights(ureq.getIdentity(), ureq.getUserSession().getRoles(), quota);
			}
		}
		return false;
	}
	
	private void doOpenView(UserRequest ureq, FolderView view) {
		this.folderView = view;
		if (FolderView.folder != folderView) {
			setCurrentContainer(rootContainer);
		}
		
		doOpenFolderView(ureq);
		updateViewUI();
		updateCommandUI(ureq);
	}
	
	private void updateViewUI() {
		folderBreadcrumb.setVisible(FolderView.folder == folderView);
		
		if (FolderView.folder == folderView) {
			viewFolderLink.setElementCssClass("active");
		} else {
			viewFolderLink.setElementCssClass(null);
		}
		if (FolderView.file == folderView) {
			viewFileLink.setElementCssClass("active");
		} else {
			viewFileLink.setElementCssClass(null);
		}
		
		flc.contextPut("searchView", FolderView.search == folderView);
		if (FolderView.search == folderView) {
			quickSearchEl.setFocus(true);
		} else {
			quickSearchEl.setValue(null);
		}
	}

	private void doOpenFolderView(UserRequest ureq) {
		FlexiTableRendererType rendererType = tableEl != null
				? tableEl.getRendererType()
				: FlexiTableRendererType.custom;
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel iconCol = new DefaultFlexiColumnModel(FolderCols.icon, new FolderIconRenderer());
		iconCol.setExportable(false);
		columnsModel.addFlexiColumnModel(iconCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.title));
		FlexiCellRenderer downloadCellRenderer = new StaticFlexiCellRenderer(null, CMD_DOWNLOAD, null, "o_icon o_icon_fw o_icon_download", translate("download"));
		DefaultFlexiColumnModel downloadCol = new DefaultFlexiColumnModel(FolderCols.download, downloadCellRenderer);
		downloadCol.setExportable(false);
		columnsModel.addFlexiColumnModel(downloadCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.createdBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FolderCols.lastModifiedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FolderCols.lastModifiedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.size));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.status, new FolderStatusCellRenderer()));
		if (FolderView.folder != folderView) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FolderCols.path));
		}
		if (versionsEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FolderCols.versions));
		}
		if (licensesEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FolderCols.license, new LicenseRenderer(getLocale())));
		}
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(FolderCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setSortable(false);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		dataModel = new FolderDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), flc);
		tableEl.setSortSettings(new FlexiTableSortOptions(true, new SortKey(FolderCols.title.name(), true)));
		tableEl.setAndLoadPersistedPreferences(ureq, "folder." + folderView.name());
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(rendererType);
		tableEl.setCssDelegate(FolderCssDelegate.DELEGATE);
		VelocityContainer rowVC = createVelocityContainer("folder_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		initBulkLinks();
		
		loadModel(ureq);
		
		if (FolderView.file == folderView) {
			initFilters();
			initFilterTabs(ureq);
		}
	}
	
	private void doQuickSearch(UserRequest ureq) {
		if (FolderView.search != folderView) {
			doOpenView(ureq, FolderView.search);
			updateCommandUI(ureq);
		} else {
			loadModel(ureq);
		}
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues creatorValues = new SelectionValues();
		dataModel.getObjects().stream()
				.map(FolderRow::getCreatedBy)
				.filter(StringHelper::containsNonWhitespace)
				.distinct()
				.forEach(suffix -> creatorValues.add(SelectionValues.entry(suffix, suffix)));
		creatorValues.sort(SelectionValues.VALUE_ASC);
		if (creatorValues.size() > 0) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("created.by"), FILTER_INITIALIZED_BY, creatorValues, true));
		}
		
		SelectionValues suffixValues = new SelectionValues();
		dataModel.getObjects().stream()
				.map(FolderRow::getFileSuffix)
				.filter(StringHelper::containsNonWhitespace)
				.distinct()
				.forEach(suffix -> suffixValues.add(SelectionValues.entry(suffix, suffix)));
		suffixValues.sort(SelectionValues.VALUE_ASC);
		if (suffixValues.size() > 0) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("table.type"), FILTER_TYPE, suffixValues, true));
		}
		
		filters.add(new FlexiTableDateRangeFilter(translate("modified.date"), FILTER_MODIFIED_DATE, true, true,
				translate("from"), translate("to"), getLocale()));
		
		filters.add(new FlexiTableTextFilter(translate("table.title"), FILTER_TITLE, true));
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(FolderStatus.locked.name(), translate("status.locked")));
		statusValues.add(SelectionValues.entry(FolderStatus.editing.name(), translate("status.editing")));
		statusValues.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("table.status"), FILTER_STATUS, statusValues, false));
		
		if (licensesEnabled) {
			SelectionValues licenseValues = new SelectionValues();
			List<LicenseType> activeLicenseTypes = licenseService.loadActiveLicenseTypes(licenseHandler);
			activeLicenseTypes.forEach(licenseType -> {
				licenseValues.add(entry(
						String.valueOf(licenseType.getKey()),
						StringHelper.escapeHtml(LicenseUIFactory.translate(licenseType, getLocale()))));
			});
			licenseValues.sort(VALUE_ASC);
			filters.add(new FlexiTableMultiSelectionFilter(translate("table.license"), FILTER_LICENSE, licenseValues, false));
		}
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(3);
		
		tabAll = FlexiFiltersTabFactory.tab(
				TAB_ID_ALL,
				translate("tab.all"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabAll);
		
		tabFromMe = FlexiFiltersTabFactory.tab(
				TAB_ID_FROM_ME,
				translate("tab.from.me"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabFromMe);
		
		tabForMe = FlexiFiltersTabFactory.tab(
				TAB_ID_FOR_ME,
				translate("tab.for.me"),
				TabSelectionBehavior.reloadData);
		tabs.add(tabForMe);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		loadModel(ureq);
	}

	private void initBulkLinks() {
		bulkDownloadButton = uifactory.addFormLink("download", flc, Link.BUTTON);
		bulkDownloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		tableEl.addBatchButton(bulkDownloadButton);
		
		bulkMoveButton = uifactory.addFormLink("move.to", flc, Link.BUTTON);
		bulkMoveButton.setIconLeftCSS("o_icon o_icon-fw o_icon_move");
		tableEl.addBatchButton(bulkMoveButton);
		
		bulkCopyButton = uifactory.addFormLink("copy.to", flc, Link.BUTTON);
		bulkCopyButton.setIconLeftCSS("o_icon o_icon-fw o_icon_duplicate");
		tableEl.addBatchButton(bulkCopyButton);
		
		bulkZipButton = uifactory.addFormLink("zip", flc, Link.BUTTON);
		bulkZipButton.setIconLeftCSS("o_icon o_icon-fw o_filetype_zip");
		tableEl.addBatchButton(bulkZipButton);
		
		bulkEmailButton = uifactory.addFormLink("email.send", flc, Link.BUTTON);
		bulkEmailButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
		tableEl.addBatchButton(bulkEmailButton);
	}

	private void loadModel(UserRequest ureq) {
		List<VFSItem> items = loadItems();
		
		String relPath = currentContainer.getRelPath();
		Map<String, VFSMetadata> metadatas = Collections.emptyMap();
		if (relPath != null) {
			List<VFSMetadata> m = vfsRepositoryService.getChildren(relPath);
			metadatas = m.stream().collect(Collectors.toMap(VFSMetadata::getFilename, v -> v, (u, v) -> u));
		}
		
		List<FolderRow> rows = new ArrayList<>(items.size());
		for (VFSItem vfsItem : items) {
			FolderRow row = new FolderRow(vfsItem);
			VFSMetadata vfsMetadata = metadatas.get(vfsItem.getName());
			row.setMetadata(vfsMetadata);
			
			String iconCssClass = vfsItem instanceof VFSContainer
					? "o_filetype_folder"
					: CSSHelper.createFiletypeIconCssClassFor(vfsItem.getName());
			row.setIconCssClass(iconCssClass);
			row.setTitle(FolderUIFactory.getDisplayName(vfsMetadata, vfsItem));
			row.setCreatedBy(FolderUIFactory.getCreatedBy(userManager, vfsMetadata));
			row.setLastModifiedDate(FolderUIFactory.getLastModifiedDate(vfsMetadata, vfsItem));
			row.setLastModifiedBy(FolderUIFactory.getLastModifiedBy(userManager, vfsMetadata));
			row.setModified(FolderUIFactory.getModified(formatter, row.getLastModifiedDate(), row.getLastModifiedBy()));
			row.setFileSuffix(FolderUIFactory.getFileSuffix(vfsMetadata, vfsItem));
			row.setTranslatedType(FolderUIFactory.getTranslatedType(getTranslator(), vfsMetadata, vfsItem));
			row.setSize(FolderUIFactory.getSize(vfsMetadata, vfsItem));
			row.setTranslatedSize(FolderUIFactory.getTranslatedSize(getTranslator(), vfsItem, row.getSize()));
			if (versionsEnabled) {
				row.setVersions(FolderUIFactory.getVersions(vfsMetadata));
			}
			if (licensesEnabled) {
				row.setLicense(vfsRepositoryService.getLicense(vfsMetadata));
				if (row.getLicense() != null) {
					row.setTranslatedLicense(LicenseUIFactory.translate(row.getLicense().getLicenseType(), getLocale()));
				}
			}
			forgeTitleLink(row);
			forgeFilePath(row);
			forgeStatus(row);
			
			rows.add(row);
		}
		
		applyFilters(rows);
		rows.forEach(row -> {
			forgeThumbnail(ureq, row);
			forgeToolsLink(row);
		});
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		updateQuotaBarUI(ureq);
	}

	private List<VFSItem> loadItems() {
		if (FolderView.folder == folderView) {
			return getCachedContainer(currentContainer).getItems(vfsFilter);
		}
		
		List<VFSItem> allItems = new ArrayList<>();
		loadItemsAndChildren(allItems, currentContainer);
		return allItems;
	}
	
	private void loadItemsAndChildren(List<VFSItem> allItems, VFSContainer vfsContainer) {
		boolean descendantsLoaded = false;
		List<VFSItem> items = null;
		VFSContainer cachedContainer = getCachedContainer(vfsContainer);
		if (VFSConstants.YES == cachedContainer.canDescendants()) {
			items = cachedContainer.getDescendants(vfsFilter);
			descendantsLoaded = true;
		}
		
		if (items == null) {
			items = vfsContainer.getItems(vfsFilter);
		}
		List<VFSItem> visibleItems = FolderView.file == folderView
				? items.stream().filter(item -> item instanceof VFSLeaf).toList()
				: items;
		allItems.addAll(visibleItems);
		
		if (!descendantsLoaded) {
			items.forEach(item -> {
				if (item instanceof VFSContainer childContainer) {
					loadItemsAndChildren(allItems, childContainer);
				}
			});
		}
	}
	
	private VFSContainer getCachedContainer(VFSContainer vfsContainer) {
		if (VFSConstants.YES == vfsContainer.canMeta()) {
			return new VFSMetadataContainer(vfsRepositoryService, true, vfsContainer);
		}
		return vfsContainer;
	}

	private void forgeStatus(FolderRow row) {
		String translatedStatus = null;
		String labels = null;
		if (row.getVfsItem() instanceof VFSContainer) {
			if (StringHelper.containsNonWhitespace(row.getTranslatedSize())) {
				labels = "<div class=\"o_folder_label o_folder_label_elements\"><i class=\"o_icon o_filetype_file\"> </i> " + row.getTranslatedSize() + "</div>";
			}
		} else {
			LockInfo lock = vfsLockManager.getLockInfo(row.getVfsItem(), row.getMetadata());
			if (lock != null && lock.getLockedBy() != null && lock.isCollaborationLock()) {
				row.setStatus(FolderStatus.editing);
				translatedStatus = translate("status.editing");
				labels = "<div class=\"o_folder_label o_folder_label_editing\"><i class=\"o_icon o_icon_user\"> </i> " + translatedStatus + "</div>";
			} else if (lock != null) {
				row.setStatus(FolderStatus.locked);
				translatedStatus = translate("status.locked");
				labels = "<div class=\"o_folder_label o_folder_label_locked\"><i class=\"o_icon o_icon_locked\"> </i> " + translatedStatus + "</div>";
			}
		}
		row.setTranslatedStatus(translatedStatus);
		row.setLabels(labels);
	}
	
	private void forgeThumbnail(UserRequest ureq, FolderRow row) {
		if (row.getVfsItem() instanceof VFSLeaf vfsLeaf && isThumbnailAvailable(row.getMetadata(), vfsLeaf)) {
			VFSLeaf thumbnail = getThumbnail(row.getMetadata(), vfsLeaf);
			if (thumbnail != null) {
				row.setThumbnailAvailable(true);
				VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
				String thumbnailUrl = registerCacheableMapper(ureq, null, thumbnailMapper);
				row.setThumbnailUrl(thumbnailUrl);
			}
		}
	}
	
	private boolean isThumbnailAvailable(VFSMetadata vfsMetadata, VFSLeaf vfsLeaf) {
		if (isAudio(vfsMetadata, vfsLeaf)) {
			return true;
		}
		return vfsRepositoryService.isThumbnailAvailable(vfsLeaf, vfsMetadata);
	}

	private VFSLeaf getThumbnail(VFSMetadata vfsMetadata, VFSLeaf vfsLeaf) {
		if (isAudio(vfsMetadata, vfsLeaf)) {
			return vfsRepositoryService.getLeafFor(avModule.getAudioWaveformUrl());
		}
		return FlexiTableRendererType.classic == tableEl.getRendererType()
				? vfsRepositoryService.getThumbnail(vfsLeaf, 30, 30, false)
				: vfsRepositoryService.getThumbnail(vfsLeaf, 1000, 650, false);
	}
	
	private boolean isAudio(VFSMetadata vfsMetadata, VFSLeaf vfsLeaf) {
		String filename = vfsMetadata != null
				? vfsMetadata.getFilename()
				: vfsLeaf.getName();
		if ("m4a".equalsIgnoreCase(FileUtils.getFileSuffix(filename))) {
			return true;
		}
		return false;
	}
	
	private void forgeTitleLink(FolderRow row) {
		if (row.getVfsItem() instanceof VFSContainer) {
			FormLink selectionLink = uifactory.addFormLink("select_" + counter++, CMD_FOLDER, "", null, flc, Link.LINK + Link.NONTRANSLATED);
			FormLink titleLink = uifactory.addFormLink("title_" + counter++, CMD_FOLDER, "", null, null, Link.NONTRANSLATED);
			
			selectionLink.setElementCssClass("o_link_plain");
			
			selectionLink.setI18nKey(StringHelper.escapeHtml(row.getTitle()));
			titleLink.setI18nKey(StringHelper.escapeHtml(row.getTitle()));
			
			selectionLink.setUserObject(row);
			titleLink.setUserObject(row);
			
			row.setSelectionItem(selectionLink);
			row.setTitleItem(titleLink);
		} else {
			String iconCSS = CSSHelper.getIcon(CSSHelper.createFiletypeIconCssClassFor(row.getVfsItem().getName()));
			String selectionText = iconCSS + " " + row.getTitle();
			StaticTextElement selectionEl = uifactory.addStaticTextElement("selection_" + counter++, null, selectionText, flc);
			selectionEl.setElementCssClass("o_nowrap");
			selectionEl.setStaticFormElement(false);
			row.setSelectionItem(selectionEl);
			
			StaticTextElement titleEl = uifactory.addStaticTextElement("title_" + counter++, null, row.getTitle(), flc);
			titleEl.setStaticFormElement(false);
			row.setTitleItem(titleEl);
		}
	}
	
	private void forgeFilePath(FolderRow row) {
		VFSItem container = row.getVfsItem().getParentContainer();
		if (container == null) {
			return;
		}
		
		String filePath =  VFSManager.getRelativeItemPath(container, rootContainer, null);
		row.setFilePath(filePath);
		
		if (FolderView.folder != folderView) {
			FormLink link = uifactory.addFormLink("path_" + counter++, CMD_PATH, "", null, null, Link.NONTRANSLATED);
			link.setI18nKey(StringHelper.escapeHtml(row.getFilePath()));
			link.setUserObject(row);
			row.setFilePathItem(link);
		}
	}
	
	private void forgeToolsLink(FolderRow row) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + counter++, "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	private void applyFilters(List<FolderRow> rows) {
		if (tableEl.getSelectedFilterTab() != null) {
			if (tableEl.getSelectedFilterTab() == tabFromMe) {
				String myUserDisplayName = userManager.getUserDisplayName(getIdentity().getKey());
				rows.removeIf(row -> row.getVfsItem() instanceof VFSContainer || !myUserDisplayName.equals(row.getCreatedBy()));
			} else {
				if (tableEl.getSelectedFilterTab() == tabForMe) {
					String myUserDisplayName = userManager.getUserDisplayName(getIdentity().getKey());
					rows.removeIf(row -> row.getVfsItem() instanceof VFSContainer || myUserDisplayName.equals(row.getCreatedBy()));
				}
			}
		}
		
		if (FolderView.search == folderView) {
			String searchValue = quickSearchEl.getValue();
			if (StringHelper.containsNonWhitespace(searchValue)) {
				List<String> searchValues = Arrays.stream(searchValue.toLowerCase().split(" ")).filter(StringHelper::containsNonWhitespace).toList();
				rows.removeIf(row -> 
						containsNot(searchValues, row.getCreatedBy()) &&
						containsNot(searchValues, row.getTitle()) &&
						containsNot(searchValues, row.getDescription()) &&
						containsNot(searchValues, row.getFilename())
						);
			}
		}
		
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (FILTER_TYPE == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					rows.removeIf(row -> row.getFileSuffix() == null || !values.contains(row.getFileSuffix()));
				}
			}
			if (FILTER_INITIALIZED_BY == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					rows.removeIf(row -> row.getCreatedBy() == null || !values.contains(row.getCreatedBy()));
				}
			}
			if (FILTER_MODIFIED_DATE  == filter.getFilter()) {
				DateRange dateRange = ((FlexiTableDateRangeFilter)filter).getDateRange();
				if (dateRange != null) {
					Date filterStart = dateRange.getStart();
					if (filterStart != null) {
						rows.removeIf(row -> row.getLastModifiedDate() == null || !filterStart.before(row.getLastModifiedDate()));
					}
					Date filterEnd = dateRange.getEnd();
					if (filterEnd != null) {
						rows.removeIf(row -> row.getLastModifiedDate() == null || !filterEnd.after(row.getLastModifiedDate()));
					}
				}
			}
			if (FILTER_TITLE == filter.getFilter()) {
				String value = ((FlexiTableTextFilter)filter).getValue();
				if (StringHelper.containsNonWhitespace(value)) {
					String valueLower = value.toLowerCase();
					rows.removeIf(row -> row.getTitle() == null || row.getTitle().toLowerCase().indexOf(valueLower) < 0);
				}
			}
			if (FILTER_STATUS == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					rows.removeIf(row -> row.getStatus() == null || !values.contains(row.getStatus().name()));
				}
			}
			if (FILTER_LICENSE == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<Long> licenseTypeKeys = values.stream().map(Long::valueOf).toList();
					rows.removeIf(row -> row.getLicense() == null || !licenseTypeKeys.contains(row.getLicense().getLicenseType().getKey()));
				}
			}
		}
	}

	private boolean containsNot(List<String> searchValues, String candidate) {
		if (StringHelper.containsNonWhitespace(candidate)) {
			String candidateLowerCase = candidate.toLowerCase();
			return searchValues.stream().noneMatch(searchValue -> candidateLowerCase.indexOf(searchValue) >= 0);
		}
		return true;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
		VFSItem vfsItem = rootContainer.resolve(path);
		if (vfsItem instanceof VFSContainer) {
			updateCurrentContainer(ureq, path);
			doOpenFolderView(ureq);
		} else if (vfsItem instanceof VFSLeaf vfsLeaf) {
			updateCurrentContainer(ureq, vfsLeaf.getParentContainer());
			doOpenFolderView(ureq);
		}
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof FolderRow folderRow) {
			if (folderRow.getSelectionItem() != null) {
				cmps.add(folderRow.getSelectionItem().getComponent());
			}
			if (folderRow.getToolsLink() != null) {
				cmps.add(folderRow.getToolsLink().getComponent());
			}
		}
		return cmps;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == folderBreadcrumb) {
			if (event instanceof PopEvent popEvent) {
				Object userObject = popEvent.getUserObject();
				if (userObject instanceof String relativePath) {
					String parentPath = relativePath.substring(0, relativePath.lastIndexOf("/"));
					updateCurrentContainer(ureq, parentPath);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				loadModel(ureq);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				selectFilterTab(ureq, ((FlexiTableFilterTabEvent)event).getTab());
				loadModel(ureq);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doUpload(ureq);
			} else if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				FolderRow row = dataModel.getObject(se.getIndex());
				if (CMD_DOWNLOAD.equals(cmd)) {
					doDownload(ureq, row);
				}
			}
		} else if (viewFolderLink == source) {
			doOpenView(ureq, FolderView.folder);
		} else if (viewFileLink == source) {
			doOpenView(ureq, FolderView.file);
		} else if (viewSearchLink == source) {
			doOpenView(ureq, FolderView.search);
		} else if (quickSearchEl == source) {
			doQuickSearch(ureq);
		} else if (quickSearchButton == source) {
			doQuickSearch(ureq);
		} else if (uploadLink == source) {
			doUpload(ureq);
		} else if (createDocumentLink == source) {
			doCreateDocument(ureq);
		} else if (createFolderLink == source) {
			doCreateFolder(ureq);
		} else if (source == recordVideoLink) {
			doRecordVideo(ureq);
		} else if (source == recordAudioLink) {
			doRecordAudio(ureq);
		} else if (source == webdavLink) {
			doShowWebdav(ureq);
		} else if (source == quotaEditLink) {
			doEditQuota(ureq);
		} else if (bulkDownloadButton == source) {
			doBulkDownload(ureq);
		} else if (bulkMoveButton == source) {
			doBulkMoveSelectFolder(ureq);
		} else if (bulkCopyButton == source) {
			doBulkCopySelectFolder(ureq);
		} else if (bulkZipButton == source) {
			doBulkZipConfirmation(ureq);
		} else if (bulkEmailButton == source) {
			doBulkEmail(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doOpenTools(ureq, folderRow, link);
			} else if (CMD_FOLDER.equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doOpenFolder(ureq, folderRow);
			} else if (CMD_PATH.equals(link.getCmd()) && link.getUserObject() instanceof FolderRow folderRow) {
				doOpenPath(ureq, folderRow);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (uploadCtrl == source) {
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (createDocumentCtrl == source) {
			if (event == Event.DONE_EVENT) {
				markNews();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (createFolderCtrl == source) {
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (recordAVController == source) {
			if (event == Event.DONE_EVENT) {
				markNews();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (webdavCtrl == source) {
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (quotaEditCtrl == source) {
			if (event == Event.CHANGED_EVENT) {
				reloadQuota();
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (metadataCtrl == source) {
			if (metadataCtrl instanceof MetadataEditController) {
				if (event == Event.DONE_EVENT) {
					markNews();
				}
			}
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (copySelectFolderCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doCopyMove(ureq,
						(Boolean) copySelectFolderCtrl.getUserObject(),
						copySelectFolderCtrl.getSelectedContainer(),
						copySelectFolderCtrl.getItemsToCopy());
			}
			cmc.deactivate();
			cleanUp();
		} else if (zipConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doZip(ureq, zipConfirmationCtrl.getFileName(), zipConfirmationCtrl.getItemsToZip());
			}
			cmc.deactivate();
			cleanUp();
		} else if (emailCtrl == source) {
			loadModel(ureq);
			cmc.deactivate();
			cleanUp();
		} else if (deleteSoftlyConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDelete(ureq, (VFSItem)deleteSoftlyConfirmationCtrl.getUserObject());
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(uploadCtrl);
		removeAsListenerAndDispose(createDocumentCtrl);
		removeAsListenerAndDispose(createFolderCtrl);
		removeAsListenerAndDispose(recordAVController);
		removeAsListenerAndDispose(webdavCtrl);
		removeAsListenerAndDispose(quotaEditCtrl);
		removeAsListenerAndDispose(metadataCtrl);
		removeAsListenerAndDispose(revisonsCtrl);
		removeAsListenerAndDispose(copySelectFolderCtrl);
		removeAsListenerAndDispose(zipConfirmationCtrl);
		removeAsListenerAndDispose(emailCtrl);
		removeAsListenerAndDispose(deleteSoftlyConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		uploadCtrl = null;
		createDocumentCtrl = null;
		createFolderCtrl = null;
		recordAVController = null;
		webdavCtrl = null;
		quotaEditCtrl = null;
		metadataCtrl = null;
		revisonsCtrl = null;
		copySelectFolderCtrl = null;
		zipConfirmationCtrl = null;
		emailCtrl = null;
		deleteSoftlyConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doQuickSearch(ureq);
	}
	
	public void updateCurrentContainer(UserRequest ureq, VFSContainer container) {
		String relativePath = VFSManager.getRelativeItemPath(container, rootContainer, "/");
		updateCurrentContainer(ureq, relativePath);
	}
	
	public void updateCurrentContainer(UserRequest ureq, String relativePath) {
		String path = relativePath;
		if (path == null) {
			path = "/";
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		
		VFSItem vfsItem = rootContainer.resolve(path);
		if (vfsItem instanceof VFSContainer vfsContainer) {
			setCurrentContainer(vfsContainer);
		} else {
			setCurrentContainer(rootContainer);
			path = "/";
		}
		
		reloadVersionsEnabled();
		updateFolderBreadcrumpUI(ureq, path);
		updateCommandUI(ureq);
		bulkEmailButton.setVisible(config.getEmailFilter().canEmail(path));
		loadModel(ureq);
		updatePathResource(ureq, path);
	}
	
	private void setCurrentContainer(VFSContainer currentContainer) {
		this.currentContainer = getCachedContainer(currentContainer);
	}
	
	private void reloadVersionsEnabled() {
		versionsEnabled = vfsVersionModule.isEnabled() && rootContainer.canVersion() == VFSConstants.YES;
	}
	
	private void updateFolderBreadcrumpUI(UserRequest ureq, String path) {
		String[] pathParts = path.split("/");
		String ralativePath = "";
		folderBreadcrumb.popUpToRootController(ureq);
		for (int i = 1; i < pathParts.length; i++) {
			String pathPart = pathParts[i];
			ralativePath += "/" + pathPart;
			folderBreadcrumb.pushController(pathPart, null, ralativePath);
		}
	}
	
	private void updatePathResource(UserRequest ureq, String path) {
		String pathParam = "path=" + path;
		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck(pathParam);
		addToHistory(ureq, ores, null);
	}

	private void doOpenFolder(UserRequest ureq, FolderRow folderRow) {
		if (isItemNotAvailable(ureq, folderRow, true)) return;
		
		if (folderRow.getVfsItem() instanceof VFSContainer vfsContainer) {
			if (FolderView.folder != folderView) {
				doOpenView(ureq, FolderView.folder);
			}
			updateCurrentContainer(ureq, vfsContainer);
		}
	}

	private void doOpenPath(UserRequest ureq, FolderRow folderRow) {
		VFSContainer parent = folderRow.getVfsItem().getParentContainer();
		if (parent == null) {
			showError("error.deleted.container");
			return;
		}
		
		if (isItemNotAvailable(ureq, parent, true)) {
			return;
		}
		
		if (FolderView.folder != folderView) {
			doOpenView(ureq, FolderView.folder);
		}
		updateCurrentContainer(ureq, parent);
	}

	private void doUpload(UserRequest ureq) {
		if (guardModalController(uploadCtrl)) return;
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.upload");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.upload.quota.exceeded");
			return;
		}
		
		removeAsListenerAndDispose(uploadCtrl);
		
		uploadCtrl = new UploadController(ureq, getWindowControl(), currentContainer, folderQuota);
		listenTo(uploadCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), uploadCtrl.getInitialComponent(),
				true, translate("upload"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateDocument(UserRequest ureq) {
		if (guardModalController(createDocumentCtrl)) return;
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.create.document");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.quota.exceeded");
			return;
		}
		
		removeAsListenerAndDispose(createDocumentCtrl);
		
		String currentContainerPath = VFSManager.getRelativeItemPath(currentContainer, rootContainer, "/");
		CreateDocumentConfig createDocumentConfig = new CreateDocumentConfig(rootContainer, currentContainer,
				currentContainerPath, config.getCustomLinkTreeModel());
		DocTemplates docTemplates = DocTemplates
				.editables(getIdentity(), ureq.getUserSession().getRoles(), getLocale(), hasMetadata(currentContainer))
				.build();
		createDocumentCtrl = new CreateDocumentController(ureq, getWindowControl(), currentContainer, docTemplates,
				createDocumentConfig);
		listenTo(createDocumentCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				createDocumentCtrl.getInitialComponent(), true, translate("document.create"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateFolder(UserRequest ureq) {
		if (guardModalController(createFolderCtrl)) return;
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.create.folder");
			updateCommandUI(ureq);
		}
		
		removeAsListenerAndDispose(createFolderCtrl);
		
		createFolderCtrl = new CreateFolderController(ureq, getWindowControl(), currentContainer);
		listenTo(createFolderCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				createFolderCtrl.getInitialComponent(), true, translate("folder.create"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRecordVideo(UserRequest ureq) {
		if (guardModalController(recordAVController)) return;
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.record.video");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.quota.exceeded");
			return;
		}
		
		removeAsListenerAndDispose(recordAVController);
		recordAVController = new RecordAVController(ureq, getWindowControl(), currentContainer, false);
		listenTo(recordAVController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				recordAVController.getInitialComponent(), true, translate("record.video"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRecordAudio(UserRequest ureq) {
		if (guardModalController(recordAVController)) return;
		if (!canEdit(currentContainer)) {
			showWarning("error.cannot.record.autio");
			updateCommandUI(ureq);
		}
		
		FolderQuota folderQuota = getFolderQuota(ureq);
		if (folderQuota.isExceeded()) {
			showWarning("error.quota.exceeded");
			return;
		}
		
		removeAsListenerAndDispose(recordAVController);
		recordAVController = new RecordAVController(ureq, getWindowControl(), currentContainer, true);
		listenTo(recordAVController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				recordAVController.getInitialComponent(), true, translate("record.audio"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doShowWebdav(UserRequest ureq) {
		if (guardModalController(webdavCtrl)) return;
		
		removeAsListenerAndDispose(webdavCtrl);
		webdavCtrl = new WebDAVController(ureq, getWindowControl());
		listenTo(webdavCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), webdavCtrl.getInitialComponent(),
				true, translate("webdav"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditQuota(UserRequest ureq) {
		if (guardModalController(quotaEditCtrl)) return;
		if (!canEditQuota(ureq)) {
			showWarning("error.cannot.edit.quota");
			updateCommandUI(ureq);
			return;
		}
		
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (inheritingContainer == null || inheritingContainer.getLocalSecurityCallback().getQuota() == null) {
			showWarning("error.cannot.edit.quota");
			updateCommandUI(ureq);
			return;
		}
		
		removeAsListenerAndDispose(quotaEditCtrl);
		
		quotaEditCtrl = quotaManager.getQuotaEditorInstance(ureq, getWindowControl(),
				inheritingContainer.getLocalSecurityCallback().getQuota().getPath(), false, true);
		if (quotaEditCtrl == null) {
			showWarning("error.cannot.edit.quota");
			updateCommandUI(ureq);
			return;
		}
		
		listenTo(quotaEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), quotaEditCtrl.getInitialComponent(),
				true, translate("quota.edit"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void reloadQuota() {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (inheritingContainer == null || inheritingContainer.getLocalSecurityCallback().getQuota() == null) {
			return;
		}
		
		Quota customQuota = quotaManager.getCustomQuota(inheritingContainer.getLocalSecurityCallback().getQuota().getPath());
		if (customQuota != null) {
			inheritingContainer.getLocalSecurityCallback().setQuota(customQuota);
		}
	}
	
	private void doOpenMetadata(UserRequest ureq, FolderRow row) {
		if (guardModalController(metadataCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		removeAsListenerAndDispose(metadataCtrl);
		
		VFSItem vfsItem = row.getVfsItem();
		String resourceUrl = getResourceURL(getWindowControl(), vfsItem);
		if (canEditMedatata(vfsItem)) {
			metadataCtrl = new MetadataEditController(ureq, getWindowControl(), vfsItem, resourceUrl);
		} else {
			metadataCtrl = new MetaInfoController(ureq, getWindowControl(), vfsItem, resourceUrl);
		}
		listenTo(metadataCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), metadataCtrl.getInitialComponent(),
				true, translate("metadata"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private String getResourceURL(WindowControl wControl, VFSItem vfsItem) {
		String path = "path=" + VFSManager.getRelativeItemPath(vfsItem, rootContainer, null);
		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck(path);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, wControl);
		return BusinessControlFactory.getInstance().getAsURIString(bwControl.getBusinessControl(), false);
	}
	
	private boolean canEdit(VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf) {
			VFSContainer parentContainer = vfsItem.getParentContainer();
			if (parentContainer != null) {
				return parentContainer.canWrite() == VFSConstants.YES;
			}
		}
		
		return vfsItem.canWrite() == VFSConstants.YES;
	}
	
	private void doCopySelectFolder(UserRequest ureq, FolderRow row) {
		doCopyMoveSelectFolder(ureq, row, false, "copy.to", "copy");
	}
	
	private void doMoveSelectFolder(UserRequest ureq, FolderRow row) {
		doCopyMoveSelectFolder(ureq, row, true, "move.to", "move");
	}
	
	private void doCopyMoveSelectFolder(UserRequest ureq, FolderRow row, boolean move, String titleI18nKey, String submitI18nKey) {
		if (guardModalController(copySelectFolderCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (!canCopy(vfsItem)) {
			return;
		}
		
		removeAsListenerAndDispose(copySelectFolderCtrl);
		
		copySelectFolderCtrl = new FolderSelectionController(ureq, getWindowControl(), rootContainer, currentContainer,
				List.of(vfsItem), submitI18nKey);
		listenTo(copySelectFolderCtrl);
		copySelectFolderCtrl.setUserObject(move);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), copySelectFolderCtrl.getInitialComponent(),
				true, translate(titleI18nKey), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCopyMove(UserRequest ureq, boolean move, VFSContainer targetContainer, List<VFSItem> itemsToCopy) {
		if (isItemNotAvailable(ureq, targetContainer, true)) return;
		
		if (!canEdit(targetContainer)) {
			showWarning("error.copy.target.read.only");
			return;
		}
		
		for (VFSItem itemToCopy : itemsToCopy) {
			if (itemToCopy instanceof VFSContainer sourceContainer) {
				if (VFSManager.isContainerDescendantOrSelf(targetContainer, sourceContainer)) {
					showWarning("error.copy.overlapping");
					loadModel(ureq);
					return;
				}
			}
			if (targetContainer.resolve(itemToCopy.getName()) != null) {
				showWarning("error.copy.overlapping");
				loadModel(ureq);
				return;
			}
			if (vfsLockManager.isLockedForMe(itemToCopy, ureq.getIdentity(), VFSLockApplicationType.vfs, null)) {
				showWarning("error.copy.locked");
				loadModel(ureq);
				return;
			}
			if (itemToCopy.canCopy() != VFSConstants.YES) {
				showWarning("error.copy.other");
				loadModel(ureq);
				return;
			}
		}
		
		VFSStatus vfsStatus = VFSConstants.SUCCESS;
		ListIterator<VFSItem> listIterator = itemsToCopy.listIterator();
		while (listIterator.hasNext() && vfsStatus == VFSConstants.SUCCESS) {
			VFSItem vfsItemToCopy = listIterator.next();
			if (!isItemNotAvailable(ureq, targetContainer, false) && canCopy(vfsItemToCopy)) {	
				VFSItem targetItem = targetContainer.resolve(vfsItemToCopy.getName());
				if (vfsItemToCopy instanceof VFSLeaf sourceLeaf && targetItem != null && targetItem.canVersion() == VFSConstants.YES) {
					boolean success = vfsRepositoryService.addVersion(sourceLeaf, ureq.getIdentity(), false, "", sourceLeaf.getInputStream());
					if (!success) {
						vfsStatus = VFSConstants.ERROR_FAILED;
					}
				} else {
					vfsStatus = targetContainer.copyFrom(vfsItemToCopy, ureq.getIdentity());
				}
				if (move && vfsStatus == VFSConstants.SUCCESS) {
					vfsItemToCopy.deleteSilently();
				}
			}
		}
		
		if (vfsStatus == VFSConstants.ERROR_QUOTA_EXCEEDED) {
			showWarning("error.copy.quota.exceeded");
		} else if (vfsStatus != VFSConstants.SUCCESS) {
			showWarning("error.copy");
		}
		
		loadModel(ureq);
		markNews();
	}
	
	private void doBulkCopySelectFolder(UserRequest ureq) {
		doBulkCopyMoveSelectFolder(ureq, false, "copy.to", "copy");
	}
	
	private void doBulkMoveSelectFolder(UserRequest ureq) {
		doBulkCopyMoveSelectFolder(ureq, true, "move.to", "move");
	}
	
	private void doBulkCopyMoveSelectFolder(UserRequest ureq, boolean move, String titleI18nKey, String submitI18nKey) {
		if (guardModalController(copySelectFolderCtrl)) return;
		if (!canEdit(currentContainer)) {
			return;
		}
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSItem> itemsToCopy = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(row -> !isItemNotAvailable(ureq, row, false))
				.filter(row -> canCopy(row.getVfsItem()))
				.map(FolderRow::getVfsItem)
				.toList();
		
		if (itemsToCopy.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		copySelectFolderCtrl = new FolderSelectionController(ureq, getWindowControl(), currentContainer,
				currentContainer, itemsToCopy, submitI18nKey);
		listenTo(copySelectFolderCtrl);
		copySelectFolderCtrl.setUserObject(move);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				copySelectFolderCtrl.getInitialComponent(), true, translate(titleI18nKey), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private boolean canCopy(VFSItem vfsItem) {
		return VFSConstants.YES == vfsItem.canCopy() && canEdit(vfsItem);
	}
	
	private boolean hasMetadata(VFSItem item) {
		if (item instanceof NamedContainerImpl namedContainer) {
			item = namedContainer.getDelegate();
		}
		if (item instanceof VFSContainer) {
			String name = item.getName();
			if (name.equals("_sharedfolder_") || name.equals("_courseelementdata")) {
				return false;
			}
		}
		return item.canMeta() == VFSConstants.YES;
	}

	private boolean canEditMedatata(VFSItem vfsItem) {
		return canEdit(vfsItem) && !vfsLockManager.isLockedForMe(vfsItem, getIdentity(), VFSLockApplicationType.vfs, null);
	}

	private void doDownload(UserRequest ureq, FolderRow row) {
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			if (hasMetadata(vfsLeaf)) {
				vfsRepositoryService.increaseDownloadCount(vfsLeaf);
			}
			VFSMediaResource resource = new VFSMediaResource(vfsLeaf);
			resource.setDownloadable(true);
			ureq.getDispatchResult().setResultingMediaResource(resource);
		} else if (vfsItem instanceof VFSContainer vfsContainer) {
			FolderZipMediaResource resource = new FolderZipMediaResource(List.of(vfsContainer));
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void doBulkDownload(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<FolderRow> selectedRows = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(row -> !isItemNotAvailable(ureq, row, false))
				.toList();
		
		if (selectedRows.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		} else if (selectedRows.size() == 1) {
			doDownload(ureq, selectedRows.get(0));
		} else {
			List<VFSItem> items = selectedRows.stream()
					.map(FolderRow::getVfsItem)
					.toList();
			FolderZipMediaResource resource = new FolderZipMediaResource(items);
			ureq.getDispatchResult().setResultingMediaResource(resource);
		}
	}
	
	private void doOpenVersions(UserRequest ureq, FolderRow row) {
		if (guardModalController(revisonsCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (!hasVersion(row.getMetadata(), vfsItem) || !canEdit(vfsItem)) {
			return;
		}
		
		removeAsListenerAndDispose(metadataCtrl);
		
		boolean locked = vfsLockManager.isLockedForMe(vfsItem, getIdentity(), VFSLockApplicationType.vfs, null);
		revisonsCtrl = new RevisionListController(ureq, getWindowControl(), vfsItem, locked);
		listenTo(revisonsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), revisonsCtrl.getInitialComponent(),
				true, translate("versions"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private boolean hasVersion(VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			if (vfsVersionModule.isEnabled() && vfsLeaf.canVersion() == VFSConstants.YES && vfsMetadata != null) {
				return vfsMetadata.getRevisionNr() > 1;
			}
		}
		return false;
	}
	
	private void doZipConfirmation(UserRequest ureq, FolderRow row) {
		if (guardModalController(zipConfirmationCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (!canZip(vfsItem)) {
			return;
		}
		
		zipConfirmationCtrl = new ZipConfirmationController(ureq, getWindowControl(), currentContainer, List.of(row.getVfsItem()));
		listenTo(zipConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				zipConfirmationCtrl.getInitialComponent(), true, translate("zip"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doZip(UserRequest ureq, String fileName, List<VFSItem> itemsToZip) {
		List<VFSItem> vfsItems = itemsToZip.stream()
				.filter(vfsItem -> !isItemNotAvailable(ureq, vfsItem, false))
				.toList();
		
		if (vfsItems.isEmpty()) {
			showError("error.zip");
			loadModel(ureq);
			return;
		}
		
		VFSLeaf zipFile = currentContainer.createChildLeaf(fileName);
		if (zipFile == null) {
			showError("error.zip");
			return;
		}
		
		if (!ZipUtil.zip(vfsItems, zipFile, new VFSSystemItemFilter(), false)) {
			zipFile.deleteSilently();
			showError("error.zip");
		} else {
			vfsRepositoryService.itemSaved(zipFile, ureq.getIdentity());
			markNews();
		}
		
		long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer);
		if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < 0) {
			zipFile.deleteSilently();
			showError("error.zip.quota.exceeded");
			loadModel(ureq);
			return;
		}
		
		loadModel(ureq);
	}
	
	private void doBulkZipConfirmation(UserRequest ureq) {
		if (guardModalController(zipConfirmationCtrl)) return;
		if (!canEdit(currentContainer)) {
			return;
		}
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSItem> itemsToZip = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(row -> !isItemNotAvailable(ureq, row, false))
				.map(FolderRow::getVfsItem)
				.toList();
		
		if (itemsToZip.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		zipConfirmationCtrl = new ZipConfirmationController(ureq, getWindowControl(), currentContainer, itemsToZip);
		listenTo(zipConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				zipConfirmationCtrl.getInitialComponent(), true, translate("zip"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private boolean canZip(VFSItem vfsItem) {
		return vfsItem instanceof VFSContainer && canEdit(currentContainer);
	}
	
	private void doUnzip(UserRequest ureq, FolderRow row) {
		if (isItemNotAvailable(ureq, row, true)) return;
		
		VFSItem vfsItem = row.getVfsItem();
		if (!canUnzip(vfsItem)) {
			showError("error.unzip");
			loadModel(ureq);
			return;
		}
		
		String zipFilenameBase = vfsItem.getName().substring(0, vfsItem.getName().length() - 4);
		String unzipContainerName = getUniqueContainerName(zipFilenameBase);
		if (unzipContainerName == null) {
			showError("error.unzip");
			loadModel(ureq);
			return;
		}
		
		VFSContainer unzipContainer = currentContainer.createChildContainer(unzipContainerName);
		if (unzipContainer == null) {
			showError("error.unzip");
			loadModel(ureq);
			return;
		}
		
		if (!ZipUtil.unzipNonStrict((VFSLeaf)vfsItem, unzipContainer, ureq.getIdentity(), versionsEnabled)) {
			unzipContainer.deleteSilently();
			showError("error.unzip");
			loadModel(ureq);
			return;
		}
		
		long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer);
		if (quotaLeftKB != Quota.UNLIMITED && quotaLeftKB < 0) {
			unzipContainer.deleteSilently();
			showError("error.unzip.quota.exceeded");
			loadModel(ureq);
			return;
		}
		
		if (unzipContainer.canMeta() == VFSConstants.YES) {
			VFSMetadata metaInfo = unzipContainer.getMetaInfo();
			if (metaInfo instanceof VFSMetadataImpl metadata) {
				metadata.setFileInitializedBy(getIdentity());
				vfsRepositoryService.updateMetadata(metaInfo);
			}
		}
		
		markNews();
		loadModel(ureq);
	}

	private boolean canUnzip(VFSItem vfsItem) {
		if (canEdit(currentContainer) && vfsItem instanceof VFSLeaf vfsLeaf) {
			return vfsLeaf.getName().toLowerCase().endsWith(".zip");
		}
		return false;
	}
	
	private void doBulkEmail(UserRequest ureq) {
		if (guardModalController(emailCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<VFSLeaf> selectedLeafs = selectedIndex.stream()
				.map(index -> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.filter(row -> !isItemNotAvailable(ureq, row, false))
				.filter(row -> config.getEmailFilter().canEmail(row.getFilePath()))
				.map(row -> { 
					if (row.getVfsItem() instanceof VFSLeaf vfsLeaf) {
						return vfsLeaf;
					}
					return null;
					})
				.filter(Objects::nonNull)
				.toList();
		
		if (selectedLeafs.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			loadModel(ureq);
			return;
		}
		
		emailCtrl = new SendDocumentsByEMailController(ureq, getWindowControl());
		listenTo(emailCtrl);
		emailCtrl.setFiles(rootContainer, selectedLeafs);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), emailCtrl.getInitialComponent(),
				true, translate("email.send"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, FolderRow row) {
		if (guardModalController(deleteSoftlyConfirmationCtrl)) return;
		if (isItemNotAvailable(ureq, row, true)) return;
		
		removeAsListenerAndDispose(deleteSoftlyConfirmationCtrl);
		
		VFSItem vfsItem = row.getVfsItem();
		if (vfsItem instanceof VFSLeaf && !canDelete(vfsItem)) {
			showWarning("error.delete.locked.leaf");
			loadModel(ureq);
			return;
		}
		
		if (vfsItem instanceof VFSContainer vfsContainer  && (!canDelete(vfsItem) && hasLockedChild(vfsContainer))) {
			showWarning("error.delete.locked.children");
			return;
		}
		
		String message = translate("file.delete.softly.confirmation.message", StringHelper.escapeHtml(vfsItem.getName()));
		deleteSoftlyConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message, null, "delete", true);
		deleteSoftlyConfirmationCtrl.setUserObject(vfsItem);
		listenTo(deleteSoftlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteSoftlyConfirmationCtrl.getInitialComponent(),
				true, translate("file.delete.softly.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doDelete(UserRequest ureq, VFSItem vfsItem) {
		if (isItemNotAvailable(ureq, vfsItem, true)) {
			return;
		}
		
		if (vfsItem instanceof VFSLeaf && !canDelete(vfsItem)) {
			showWarning("error.delete.locked.leaf");
			loadModel(ureq);
			return;
		}
		
		if (vfsItem instanceof VFSContainer vfsContainer  && (!canDelete(vfsItem) && hasLockedChild(vfsContainer))) {
			showWarning("error.delete.locked.children");
			return;
		}
		
		if (versionsEnabled && vfsItem.canVersion() == VFSConstants.YES) {
			// Move to trash
			vfsItem.delete();
		} else {
			vfsItem.deleteSilently();
		}
		
		markNews();
		loadModel(ureq);
	}

	private boolean canDelete(VFSItem vfsItem) {
		return vfsItem.canDelete() == VFSConstants.YES && !vfsLockManager.isLockedForMe(vfsItem, getIdentity(), VFSLockApplicationType.vfs, null);
	}
	
	private boolean hasLockedChild(VFSContainer vfsContainer) {
		for (VFSItem vfsItem : vfsContainer.getItems()) {
			if (vfsLockManager.isLockedForMe(vfsItem, getIdentity(), VFSLockApplicationType.vfs, null)) {
				return true;
			}
			if (vfsItem instanceof VFSContainer vfsChildContainer && hasLockedChild(vfsChildContainer)) {
				return true;
			}
		}
		return false;
	}

	private boolean isItemNotAvailable(UserRequest ureq, FolderRow row, boolean showDeletedMessage) {
		return isItemNotAvailable(ureq, row.getVfsItem(), showDeletedMessage);
	}

	private boolean isItemNotAvailable(UserRequest ureq, VFSItem vfsItem, boolean showDeletedMessage) {
		if (!vfsItem.exists()) {
			if (showDeletedMessage) {
				if (vfsItem instanceof VFSContainer) {
					showError("error.deleted.container");
				} else {
					showError("error.deleted.leaf");
				}
				loadModel(ureq);
			}
			return true;
		}
		return false;
	}

	private void updateQuotaBarUI(UserRequest ureq) {
		quotaBar.setQuota(getFolderQuota(ureq));
	}
	
	private FolderQuota getFolderQuota(UserRequest ureq) {
		Quota quota = null;
		long actualUsage = 0;
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		if (inheritingContainer != null) {
			quota = inheritingContainer.getLocalSecurityCallback().getQuota();
			if (quota != null && Quota.UNLIMITED != quota.getQuotaKB()) {
				actualUsage = VFSManager.getUsageKB(inheritingContainer);
			}
		}
		
		return new FolderQuota(ureq, quota, actualUsage);
	}
	
	private String getUniqueContainerName(String baseContainerName) {
		String uniqueContainerName = baseContainerName;
		for (int i=1; i<999; i++) {
			VFSItem item = currentContainer.resolve(uniqueContainerName);
			if (item == null) {
				return uniqueContainerName;
			}
			uniqueContainerName = baseContainerName + "_" + i;
		}
		return null;
	}
	
	private void markNews() {
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(rootContainer);
		VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
		if (secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				notificationsManager.markPublisherNews(subsContext, getIdentity(), true);
			}
		}
	}
	
	private void doOpenTools(UserRequest ureq, FolderRow row, FormLink link) {
		addToHistory(ureq, this);
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		
		if (isItemNotAvailable(ureq, row, true)) {
			return;
		}
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);	

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		
		private final FolderRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, FolderRow row) {
			super(ureq, wControl);
			setTranslator(Util.createPackageTranslator(DocEditorController.class, getLocale(), getTranslator()));
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			boolean divider = false;
			VFSItem vfsItem = row.getVfsItem();
			addLink("download", CMD_DOWNLOAD, "o_icon o_icon-fw o_icon_download");
			
			if (canCopy(vfsItem)) {
				addLink("move.to", CMD_MOVE, "o_icon o_icon-fw o_icon_move");
				addLink("copy.to", CMD_COPY, "o_icon o_icon-fw o_icon_duplicate");
				divider = true;
			}
			
			if (hasMetadata(vfsItem)) {
				addLink("metadata", CMD_METADATA, "o_icon o_icon-fw o_icon_metadata");
				divider = true;
			}
			if (hasVersion(row.getMetadata(), vfsItem) && canEdit(vfsItem)) {
				addLink("versions", CMD_VERSION, "o_icon o_icon-fw o_icon_version");
				divider = true;
			}
			if (canZip(vfsItem)) {
				addLink("zip", CMD_ZIP, "o_icon o_icon-fw o_filetype_zip");
				divider = true;
			}
			if (canUnzip(vfsItem)) {
				addLink("unzip", CMD_UNZIP, "o_icon o_icon-fw o_filetype_zip");
				divider = true;
			}
			
			mainVC.contextPut("divider", divider);
			
			if (canDelete(vfsItem)) {
				addLink("delete", CMD_DELETE, "o_icon o_icon-fw o_icon_delete_item");
			}
			
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if (source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				if (CMD_DOWNLOAD.equals(cmd)) {
					doDownload(ureq, row);
				} else if (CMD_MOVE.equals(cmd)) {
					doMoveSelectFolder(ureq, row);
				} else if (CMD_COPY.equals(cmd)) {
					doCopySelectFolder(ureq, row);
				} else if (CMD_METADATA.equals(cmd)) {
					doOpenMetadata(ureq, row);
				} else if (CMD_VERSION.equals(cmd)) {
					doOpenVersions(ureq, row);
				} else if (CMD_ZIP.equals(cmd)) {
					doZipConfirmation(ureq, row);
				} else if (CMD_UNZIP.equals(cmd)) {
					doUnzip(ureq, row);
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row);
				}
			}
		}
	}
	
	private static final class FolderCssDelegate extends DefaultFlexiTableCssDelegate {
		
		private static final FolderCssDelegate DELEGATE = new FolderCssDelegate();
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			if (FlexiTableRendererType.custom == type) {
				return "o_folder_table o_block_top";
			}
			return null;
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_folder_row";
		}
	}

}