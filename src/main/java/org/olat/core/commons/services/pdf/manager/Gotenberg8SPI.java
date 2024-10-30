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
package org.olat.core.commons.services.pdf.manager;

import java.io.OutputStream;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.ui.Gotenberg8SettingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Slightly adapted Gotenberg service provider with optimization for A4, margin in millimeter.
 * 
 * Initial date: 30 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class Gotenberg8SPI extends AbstractPdfSPI {

	private static final Logger log = Tracing.createLoggerFor(Gotenberg8SPI.class);
	
	private static final String GOTENBERG_URL = "gotenberg.url";

	@Value("${gotenberg.url:@null}")
	private String serviceUrl;
	
	@Autowired
	public Gotenberg8SPI(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public String getId() {
		return "Gotenberg8";
	}
	
	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		serviceUrl = getStringPropertyValue(GOTENBERG_URL, serviceUrl);
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
		setStringProperty(GOTENBERG_URL, serviceUrl, true);
	}

	@Override
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new Gotenberg8SettingsController(ureq, wControl);
	}

	@Override
	protected void render(String key, String rootFilename, PdfOutputOptions options, OutputStream out) {
		try(CloseableHttpClient httpclient = httpClientService.createHttpClient()) {
			
			StringBuilder sb = new StringBuilder(128);
			sb.append(serviceUrl);
			if(!serviceUrl.endsWith("/")) {
				sb.append("/");
			}
			sb.append("forms/chromium/convert/url");
			HttpPost post = new HttpPost(sb.toString());
			
			RequestConfig config = RequestConfig.copy(RequestConfig.DEFAULT)
				.setCookieSpec(CookieSpecs.DEFAULT)
				.build();
			post.setConfig(config);
			post.addHeader("Accept", "application/pdf");
			post.addHeader("Accept-Language", "en");
			
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
					.addTextBody("url", Settings.getServerContextPathURI() + "/pdfd/" + key + "/" + rootFilename)
					.addTextBody("preferCssPageSize", "true")
					.addTextBody("paperWidth", "210mm")
					.addTextBody("paperHeight", "297mm");

			if(options != null) {
				if(options.getEmulatedMediaType() != null) {
					entityBuilder = entityBuilder.addTextBody("emulatedMediaType", options.getEmulatedMediaType().name());
				}
				if(options.getMarginTop() != null) {
					entityBuilder = entityBuilder.addTextBody("marginTop", options.getMarginTop() + "mm");
				}
				if(options.getMarginBottom() != null) {
					entityBuilder = entityBuilder.addTextBody("marginBottom", options.getMarginBottom() + "mm");
				}
				if(options.getMarginLeft() != null) {
					entityBuilder = entityBuilder.addTextBody("marginLeft", options.getMarginLeft() + "mm");
				}
				if(options.getMarginRight() != null) {
					entityBuilder = entityBuilder.addTextBody("marginRight", options.getMarginRight() + "mm");
				}

				if(options.getPageRange() != null) {
					int pageStart = options.getPageRange().start();
					int pageEnd = options.getPageRange().end();
					if(pageStart == pageEnd) {
						entityBuilder = entityBuilder.addTextBody("nativePageRanges", Integer.toString(pageStart));
					} else {
						entityBuilder = entityBuilder.addTextBody("nativePageRanges", pageStart + "-" + pageEnd);
					}
				}
			}
			post.setEntity(entityBuilder.build());

			executeRequest(httpclient, post, out);
		} catch(Exception e) {
			log.error("", e);
		}
	}
}
