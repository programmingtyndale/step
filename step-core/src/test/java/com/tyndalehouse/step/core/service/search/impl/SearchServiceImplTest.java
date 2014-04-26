package com.tyndalehouse.step.core.service.search.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.tyndalehouse.step.core.models.AvailableFeatures;
import com.tyndalehouse.step.core.models.LookupOption;
import com.tyndalehouse.step.core.service.PassageOptionsValidationService;
import com.tyndalehouse.step.core.service.impl.LexiconDefinitionServiceImpl;
import com.tyndalehouse.step.core.service.impl.SearchQuery;
import com.tyndalehouse.step.core.service.impl.TimelineServiceImpl;
import com.tyndalehouse.step.core.service.jsword.JSwordMetadataService;
import com.tyndalehouse.step.core.service.jsword.JSwordModuleService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyndalehouse.step.core.data.entities.impl.TestEntityManager;
import com.tyndalehouse.step.core.models.search.SearchEntry;
import com.tyndalehouse.step.core.models.search.SearchResult;
import com.tyndalehouse.step.core.models.search.SubjectHeadingSearchEntry;
import com.tyndalehouse.step.core.models.search.TimelineEventSearchEntry;
import com.tyndalehouse.step.core.models.search.VerseSearchEntry;
import com.tyndalehouse.step.core.service.helpers.VersionResolver;
import com.tyndalehouse.step.core.service.jsword.JSwordVersificationService;
import com.tyndalehouse.step.core.service.jsword.impl.JSwordPassageServiceImpl;
import com.tyndalehouse.step.core.service.jsword.impl.JSwordSearchServiceImpl;
import com.tyndalehouse.step.core.utils.TestUtils;

/**
 * Search service testing
 *
 * @author chrisburrell
 */
public class SearchServiceImplTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceImplTest.class);
    private TestEntityManager entityManager;
    private SubjectSearchServiceImpl subjects;
    private SearchServiceImpl searchServiceUnderTest;

    @Before
    public void setUp() {
        entityManager = new TestEntityManager();
        searchServiceUnderTest = getSearchServiceUnderTest();
    }

    /**
     * Random tests
     */
    @Test
    public void testMultiVersionSearch() {
        final List<SearchEntry> results = this.searchServiceUnderTest.search(
                new SearchQuery("t=elijah", new String[]{"ESV", "KJV", "ASV"}, "false", 0, 1, 1)).getResults();
        assertFalse(results.isEmpty());
    }

    /**
     * test exact strong match
     */
    @Test
    public void testSubjectSearch() {

        final SearchResult searchSubject = this.searchServiceUnderTest.search(
                new SearchQuery("s=elijah", new String[]{"ESV"}, "false", 0, 1, 1));

        final List<SearchEntry> entries = ((SubjectHeadingSearchEntry) searchSubject.getResults().get(0))
                .getHeadingsSearch().getResults();
        for (final SearchEntry e : entries) {
            LOGGER.debug(((VerseSearchEntry) e).getPreview());

        }
        assertTrue(searchSubject.getResults().size() > 0);
    }

    @Test
    public void testExpandingToLucene() {
        assertEquals("+(expandedReferences:Matt.*)", this.subjects.getInputReferenceForNaveSearch("ESV", "Mat").getValue());
        assertEquals("+(expandedReferences:Matt.1.*)", this.subjects.getInputReferenceForNaveSearch("ESV", "Mat 1").getValue());
        assertEquals("+(expandedReferences:Matt.1.1)", this.subjects.getInputReferenceForNaveSearch("ESV", "Mat 1:1").getValue());
        assertEquals("+(expandedReferences:Matt.1.2 expandedReferences:Matt.1.3)", this.subjects.getInputReferenceForNaveSearch("ESV", "Mat 1:2-3").getValue());
        assertEquals("+(expandedReferences:Obad.*)", this.subjects.getInputReferenceForNaveSearch("ESV", "Obadiah").getValue());
        assertEquals("+(expandedReferences:Obad.1.2)", this.subjects.getInputReferenceForNaveSearch("ESV", "Obadiah 2").getValue());
        assertEquals("+(expandedReferences:Gen.* expandedReferences:Exod.* )", this.subjects.getInputReferenceForNaveSearch("ESV", "Gen-Exo").getValue());
        assertEquals("+(expandedReferences:Gen.* expandedReferences:Exod.* expandedReferences:Lev.* expandedReferences:Mark.* )", this.subjects.getInputReferenceForNaveSearch("ESV", "Gen-Lev ; Mark").getValue());
    }

    /**
     * @return the search service to test
     */
    private SearchServiceImpl getSearchServiceUnderTest() {
        final JSwordMetadataService meta = mock(JSwordMetadataService.class);
        final JSwordModuleService module = mock(JSwordModuleService.class);
        final JSwordVersificationService versificationService = TestUtils.mockVersificationService();
        final PassageOptionsValidationService optionsValidationService = mock(PassageOptionsValidationService.class);
        final JSwordPassageServiceImpl jsword = new JSwordPassageServiceImpl(versificationService, null,
                null, null, mock(VersionResolver.class), optionsValidationService);

        when(optionsValidationService.getAvailableFeaturesForVersion(any(String.class), any(List.class), any(String.class)))
                .thenReturn(new AvailableFeatures());

        when(module.isInstalled("ESV")).thenReturn(true);
        when(module.isIndexed(any(String.class))).thenReturn(true);
        when(meta.supportsFeature(any(String.class), any(LookupOption.class))).thenReturn(true);

        final JSwordSearchServiceImpl jswordSearch = new JSwordSearchServiceImpl(versificationService, null, jsword);
        subjects = new SubjectSearchServiceImpl(entityManager,
                jswordSearch, meta, module, versificationService);
        return new SearchServiceImpl(jswordSearch, meta, versificationService, subjects, new TimelineServiceImpl(entityManager, jsword), null, entityManager, TestUtils.mockVersionResolver(),
                mock(LexiconDefinitionServiceImpl.class), null
        );
    }
}
