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
package org.olat.modules.oaipmh.manager;


import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.modules.oaipmh.DataProvider;
import org.olat.modules.oaipmh.OAIPmhMetadataProvider;
import org.olat.modules.oaipmh.OAIService;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.model.Granularity;
import org.olat.modules.oaipmh.common.services.impl.SimpleResumptionTokenFormat;
import org.olat.modules.oaipmh.common.services.impl.UTCDateProvider;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.modules.oaipmh.dataprovider.builder.OAIRequestParametersBuilder;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.MetadataFormat;
import org.olat.modules.oaipmh.dataprovider.model.MetadataItems;
import org.olat.modules.oaipmh.dataprovider.repository.MetadataItemRepository;
import org.olat.modules.oaipmh.dataprovider.repository.MetadataSetRepository;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;
import org.olat.modules.oaipmh.dataprovider.repository.RepositoryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class OAIServiceImpl implements OAIService {

	private static final String METADATA_DEFAULT_PREFIX = "oai_dc";

	@Autowired
	private List<OAIPmhMetadataProvider> metadataProviders;

	@Override
	public MediaResource handleOAIRequest(
			String requestVerbParameter,
			String requestIdentifierParameter,
			String requestMetadataPrefixParameter,
			String requestResumptionTokenParameter,
			String requestFromParameter,
			String requestUntilParameter,
			String requestSetParameter) {

		MetadataSetRepository setRepository = new MetadataSetRepository();
		MetadataItemRepository itemRepository = new MetadataItemRepository();
		RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration().withDefaults();

		Context context = new Context();
		Repository repository = new Repository()
				.withSetRepository(setRepository)
				.withItemRepository(itemRepository)
				.withResumptionTokenFormatter(new SimpleResumptionTokenFormat())
				.withConfiguration(repositoryConfiguration);

		StringMediaResource mr = new StringMediaResource();
		String result = "";

		if (!StringHelper.containsNonWhitespace(requestMetadataPrefixParameter)
				&& requestResumptionTokenParameter == null
				&& !requestVerbParameter.equalsIgnoreCase("listmetadataformats")
				&& !requestVerbParameter.equalsIgnoreCase("identify")
				&& !requestVerbParameter.equalsIgnoreCase("listsets")) {
			requestMetadataPrefixParameter = METADATA_DEFAULT_PREFIX;
		}

		if (StringHelper.containsNonWhitespace(requestMetadataPrefixParameter)) {
			context.withMetadataFormat(requestMetadataPrefixParameter, MetadataFormat.identity());
		} else {
			context.withMetadataFormat(METADATA_DEFAULT_PREFIX, MetadataFormat.identity());
		}

		if (requestSetParameter != null) {
			String[] setSpec = requestSetParameter.split(":");
			setRepository.withSet(setSpec[1], requestSetParameter);
		}

		DataProvider dataProvider = new DataProvider(context, repository);

		try {
			Date fromParameter = null;
			Date untilParameter = null;
			if (requestFromParameter != null) {
				fromParameter = new UTCDateProvider().parse(requestFromParameter, Granularity.Day);
			}
			if (requestUntilParameter != null) {
				untilParameter = new UTCDateProvider().parse(requestUntilParameter, Granularity.Day);
			}

			OAIRequestParametersBuilder requestBuilder = new OAIRequestParametersBuilder();
			requestBuilder.withVerb(requestVerbParameter)
					.withFrom(fromParameter)
					.withUntil(untilParameter)
					.withIdentifier(requestIdentifierParameter)
					.withMetadataPrefix(requestMetadataPrefixParameter)
					.withResumptionToken(requestResumptionTokenParameter)
					.withSet(requestSetParameter);

			List<MetadataItems> metadataItems =
					repositoryItems(requestMetadataPrefixParameter, setRepository);
			itemRepository.withRepositoryItems(metadataItems);

			result = write(dataProvider.handle(requestBuilder));
		} catch (OAIException | XMLStreamException | XmlWriteException | ParseException e) {
			throw new RuntimeException(e);
		}

		mr.setContentType("application/xml");
		mr.setEncoding("UTF-8");
		mr.setData(result);

		return mr;
	}

	private List<MetadataItems> repositoryItems(String metadataprefix, MetadataSetRepository setRepository) {
		OAIPmhMetadataProvider provider =
				getMetadataProvider(metadataprefix).orElse(getMetadataProvider(METADATA_DEFAULT_PREFIX).get());
		return provider.getMetadata(setRepository);
	}

	private Optional<OAIPmhMetadataProvider> getMetadataProvider(String metadataprefix) {
		return metadataProviders.stream().filter(m -> m.getMetadataPrefix().equals(metadataprefix)).findFirst();
	}


	private String write(XmlWritable handle) throws XMLStreamException, XmlWriteException {
		return XmlWriter.toString(writer -> writer.write(handle));
	}
}
