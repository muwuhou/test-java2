import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.testng.annotations.Test;


public class TestLucene {
  private static final Path INDEX_DIR = Paths.get("/tmp/lucene0");

  private static List<IndexableField> DOC1 = ImmutableList.of(
      new StringField("name", "tom", Field.Store.NO),
      new StringField("name", "chen", Field.Store.NO),
      new StringField("id", "001", Field.Store.NO),
      new StringField("title", "engineer", Field.Store.NO),
      new StringField("title", "developer", Field.Store.NO),
      new StringField("title", "swe", Field.Store.NO),
      new StringField("gender", "m", Field.Store.NO)
  );

  private static List<IndexableField> DOC2 = ImmutableList.of(
      new StringField("name", "jack", Field.Store.NO),
      new StringField("name", "chen", Field.Store.NO),
      new StringField("id", "002", Field.Store.NO),
      new StringField("title", "artist", Field.Store.NO),
      new StringField("title", "painter", Field.Store.NO),
      new StringField("gender", "m", Field.Store.NO)
  );

  private static List<IndexableField> DOC3 = ImmutableList.of(
      new StringField("name", "jane", Field.Store.NO),
      new StringField("name", "zhang", Field.Store.NO),
      new StringField("id", "003", Field.Store.NO),
      new StringField("title", "administrator", Field.Store.NO),
      new StringField("title", "assistant", Field.Store.NO),
      new StringField("title", "painter", Field.Store.NO),
      new StringField("gender", "f", Field.Store.NO)
  );

  @Test
  public void testIndexWriter() throws IOException {
    var config = new IndexWriterConfig();
    Directory outputDir = new MMapDirectory(INDEX_DIR);
    try (var writer = new IndexWriter(outputDir, config)) {
      // writer.deleteDocuments(new Term("id", "001"));
      writer.addDocument(DOC1);
      writer.addDocument(DOC2);
      writer.addDocument(DOC3);
      writer.flush();

      // merge segment
      // writer.forceMerge(1);
    }
  }

  @Test
  public void testIndexReader() throws IOException {
    Directory indexDir = new MMapDirectory(INDEX_DIR);
    try (IndexReader reader = DirectoryReader.open(indexDir)) {
      System.out.printf("IndexReader: %s\n", reader);
      for (LeafReaderContext ctx: reader.leaves()) {
        LeafReader leafReader = ctx.reader();
        System.out.println("==========================");
        dumpLeafIndex(leafReader);
      }
    }
  }

  @Test
  public void testIndexSearcher() throws IOException {
    Directory indexDir = new MMapDirectory(INDEX_DIR);
    try (IndexReader reader = DirectoryReader.open(indexDir)) {
      IndexSearcher searcher = new IndexSearcher(reader);
      Query query = new TermQuery(new Term("name", "jack"));
      System.out.printf("Search with query: %s\n", query);
      TopDocs topDocs = searcher.search(query, 10);
      System.out.printf("total hit: %d\n", topDocs.totalHits);
      for (var doc: topDocs.scoreDocs) {
        System.out.printf("\t doc: %d, score: %f\n", doc.doc, doc.score);
      }
    }
  }

  private void dumpLeafIndex(LeafReader leafReader) throws IOException {
    List<String> fields = new ArrayList<>();

    System.out.printf("Dump of leaf reader: %s\n", leafReader);
    leafReader.getFieldInfos().forEach((FieldInfo fi) -> {
      System.out.printf("Field: %s, hasPayload: %s\n", fi.name, fi.hasPayloads());
      fields.add(fi.name);
    });
    for (String field: fields) {
      Terms terms = leafReader.terms(field);
      System.out.printf("Field %s has %s terms:\n", field, terms.size());
      TermsEnum termsEnum = terms.iterator();
      BytesRef bytesRef = termsEnum.next();
      while (bytesRef != null) {
        String term = bytesRef.utf8ToString();
        System.out.printf("\tTerm: %s:%s - %s\n", field, term, dumpPostingList(termsEnum.postings(null)));
        bytesRef = termsEnum.next();
      }
    }
  }

  private List<Integer> dumpPostingList(PostingsEnum postingList) throws IOException {
    int docId = postingList.nextDoc();
    List<Integer> result = new ArrayList<>();
    while (docId != DocIdSetIterator.NO_MORE_DOCS) {
      result.add(docId);
      docId = postingList.nextDoc();
    }
    return result;
  }
}
