package de.intranda.goobi.plugins;

import java.io.IOException;

/**
 * This file is part of a plugin for Goobi - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;

@PluginImplementation
@Log4j2
public class HerisStepPlugin implements IStepPluginVersion2 {
    private static final String HERIS_VOCABULARY_NAME = "HERIS";
    private static final String HERIS_SEARCH_FIELD_NAME = "HERIS-ID";

    @Getter
    private String title = "intranda_step_heris";
    @Getter
    private Step step;

    private Process process;

    @Getter
    private String value;
    @Getter
    private boolean allowTaskFinishButtons;

    @Getter
    private PluginGuiType pluginGuiType = PluginGuiType.NONE;

    @Override
    public void initialize(Step step, String returnPath) {
        this.step = step;
        process = step.getProzess();
    }

    @Override
    public String getPagePath() {
        return null;
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String cancel() {
        return null;
    }

    @Override
    public String finish() {
        return null;
    }

    @Override
    public int getInterfaceVersion() {
        return 0;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public boolean execute() {
        PluginReturnValue ret = run();
        return ret != PluginReturnValue.ERROR;
    }

    @Override
    public PluginReturnValue run() {
        Map<String, String> metadataFieldMap = new HashMap<>();
        metadataFieldMap.put("Alte Objekt-ID", "DMDBID");
        metadataFieldMap.put("Gehört zu alter Objekt-ID", "ParentElement");
        metadataFieldMap.put("Katalogtitel", "TitleDocMain");
        metadataFieldMap.put("Typ", "HerisType");
        metadataFieldMap.put("Hauptkategorie grob", "MainCategory1");
        metadataFieldMap.put("Hauptkategorie mittel", "MainCategory2");
        metadataFieldMap.put("Hauptkategorie fein", "MainCategory3");
        metadataFieldMap.put("Gemeinden politisch (lt. Katastralgemeinden)", "PoliticalCommunity");
        metadataFieldMap.put("Katastralgemeinde", "CadastralCommune");
        metadataFieldMap.put("Bezirk", "PoliticalDistrict");
        metadataFieldMap.put("Bundesland", "FederalState");
        metadataFieldMap.put("Grundstücksnummern", "PropertyNumber");
        metadataFieldMap.put("Bauzeit von", "ConstructionTimeFrom");
        metadataFieldMap.put("Bauzeit bis", "ConstructionTimeTo");
        metadataFieldMap.put("Publiziert", "Published");
        metadataFieldMap.put("Straße", "Street");
        metadataFieldMap.put("Hausnummer", "StreetNumber");
        metadataFieldMap.put("PLZ", "ZIPCode");
        metadataFieldMap.put("Zusatztext aus Adresse", "AdditionalAddressText");
        metadataFieldMap.put("Weitere Adressen", "OtherAddress");
        metadataFieldMap.put("Gehört zu HERIS-ID", "ParentElement");
        metadataFieldMap.put("Ort", "Community");
        metadataFieldMap.put("Staat", "Country");

        String category1 = null;
        String category2 = null;
        String category3 = null;

        try {
            // open mets file
            Prefs prefs = process.getRegelsatz().getPreferences();

            MetsMods mm = new MetsMods(prefs);
            mm.read(process.getMetadataFilePath());

            DocStruct logical = mm.getDigitalDocument().getLogicalDocStruct();

            // get heris id
            List<? extends Metadata> herisMetadata = logical.getAllMetadataByType(prefs.getMetadataTypeByName("HerisID"));
            // continue, when no heris id exists
            if (herisMetadata == null || herisMetadata.isEmpty()) {
                return PluginReturnValue.FINISH;
            }

            String herisId = herisMetadata.get(0).getValue();

            // get heris record
            VocabularyAPIManager vocabularyAPIManager = VocabularyAPIManager.getInstance();
            Vocabulary vocabulary = vocabularyAPIManager.vocabularies().findByName(HERIS_VOCABULARY_NAME);
            VocabularySchema schema = vocabularyAPIManager.vocabularySchemas().get(vocabulary.getSchemaId());
            Optional<Long> herisFieldId = schema.getDefinitions().stream()
                    .filter(d -> d.getName().equals(HERIS_SEARCH_FIELD_NAME))
                    .map(FieldDefinition::getId)
                    .findFirst();
            Map<String, Long> fieldNameIdMap = new HashMap<>();
            Map<Long, String> idFieldNameMap = new HashMap<>();
            schema.getDefinitions().forEach(d -> {
                fieldNameIdMap.put(d.getName(), d.getId());
                idFieldNameMap.put(d.getId(), d.getName());
            });

            if (herisFieldId.isEmpty()) {
                return PluginReturnValue.FINISH;
            }

            List<ExtendedVocabularyRecord> records = vocabularyAPIManager.vocabularyRecords()
                    .list(vocabulary.getId())
                    .search(herisFieldId.get() + ":" + herisId)
                    .all()
                    .request()
                    .getContent();

            // continue, when no record exists
            if (records == null || records.isEmpty()) {
                return PluginReturnValue.FINISH;
            }

            ExtendedVocabularyRecord result = records.get(0);

            // replace metadata with data from heris
            for (FieldInstance field : result.getFields()) {
                String fieldName = idFieldNameMap.get(field.getDefinitionId());
                String fieldValue = extractValueOfField(field);

                if (fieldName.equals("Hauptkategorie grob")) {
                    category1 = fieldValue;
                } else if (fieldName.equals("Hauptkategorie mittel")) {
                    category2 = fieldValue;
                } else if (fieldName.equals("Hauptkategorie fein")) {
                    category3 = fieldValue;
                }

                // find metadata type for field name
                String metadataName = metadataFieldMap.get(fieldName);
                if (metadataName == null) {
                    continue;
                }
                MetadataType mdt = prefs.getMetadataTypeByName(metadataName);

                // check if metadata already exists
                List<? extends Metadata> metadata = logical.getAllMetadataByType(mdt);

                // if no, create new metadata
                if (metadata == null || metadata.isEmpty()) {
                    try {
                        Metadata md = new Metadata(mdt);
                        md.setValue(fieldValue);
                        logical.addMetadata(md);
                    } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                        log.error(e);
                    }
                } else {
                    // if yes, replace value
                    metadata.get(0).setValue(fieldValue);
                }
            }
            StringBuilder categories = new StringBuilder();

            if (StringUtils.isNotBlank(category1)) {
                categories.append(category1.replace("#", ""));
            }
            if (StringUtils.isNotBlank(category2)) {
                categories.append("#");
                categories.append(category2);
            }
            if (StringUtils.isNotBlank(category3)) {
                categories.append("#");
                categories.append(category3);
            }
            if (categories.length() > 0) {
                MetadataType mdt = prefs.getMetadataTypeByName("_CategoryCombined");
                List<? extends Metadata> metadata = logical.getAllMetadataByType(mdt);
                if (metadata == null || metadata.isEmpty()) {
                    try {
                        Metadata md = new Metadata(mdt);
                        md.setValue(categories.toString());
                        logical.addMetadata(md);
                    } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                        log.error(e);
                    }
                } else {
                    // if yes, replace value
                    metadata.get(0).setValue(categories.toString());
                }
            }

            // save mets file
            mm.write(process.getMetadataFilePath());

        } catch (ReadException | PreferencesException | WriteException | IOException | SwapException e) {
            log.error(e);
        }

        return PluginReturnValue.FINISH;
    }

    private String extractValueOfField(FieldInstance field) {
        return field.getValues().stream()// Assume there are no multi-values
                .flatMap(v -> v.getTranslations().stream()) // Assume there are no translations
                .map(TranslationInstance::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No value found for record field"));
    }
}
