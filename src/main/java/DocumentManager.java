import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */

public class DocumentManager {
    /** public static void main(String[] args) {
     * DocumentManager documentManager = new DocumentManager();

        Author author = Author.builder()
                .id("1")
                .name("John Doe")
                .build();

        Document document = Document.builder()
                .title("Sample Title")
                .content("Sample Content")
                .author(author)
                .created(Instant.now())
                .build();

        Document savedDocument = documentManager.save(document);
        System.out.println("Сохраненный документ: " + savedDocument);

        Optional<Document> foundDocument = documentManager.findById(savedDocument.getId());
        System.out.println("Найденный документ: " + foundDocument.orElse(null));

        // Поиск документов с помощью запроса
        SearchRequest request = SearchRequest.builder()
                .titlePrefixes(Arrays.asList("Sample"))
                .containsContents(Arrays.asList("Content"))
                .authorIds(Arrays.asList("1"))
                .createdFrom(Instant.now().minusSeconds(3600))
                .createdTo(Instant.now().plusSeconds(3600))
                .build();

        List<Document> searchResults = documentManager.search(request);
        System.out.println("Результаты поиска: " + searchResults);
    }
     */

    private final Map<String, Document> documents = new ConcurrentHashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }
        // Save the document to the storage
        documents.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        // Stream through documents and apply search criteria
        return documents.values().stream()
                .filter(document -> matchesSearchRequest(document, request))
                .collect(Collectors.toList());
    }

    private boolean matchesSearchRequest(Document document, SearchRequest request) {
        // Check title prefixes
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            boolean matchesTitle = request.getTitlePrefixes().stream()
                    .anyMatch(prefix -> document.getTitle().startsWith(prefix));
            if (!matchesTitle) {
                return false;
            }
        }

        // Check content contains
        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            boolean matchesContent = request.getContainsContents().stream()
                    .anyMatch(content -> document.getContent().contains(content));
            if (!matchesContent) {
                return false;
            }
        }

        // Check author IDs
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            if (!request.getAuthorIds().contains(document.getAuthor().getId())) {
                return false;
            }
        }

        // Check created time range
        if (request.getCreatedFrom() != null && document.getCreated().isBefore(request.getCreatedFrom())) {
            return false;
        }
        if (request.getCreatedTo() != null && document.getCreated().isAfter(request.getCreatedTo())) {
            return false;
        }

        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
