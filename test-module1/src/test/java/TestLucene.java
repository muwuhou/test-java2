import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CodecReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.SlowCodecReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;
import org.testng.annotations.Test;


public class TestLucene {
  private static final Path INDEX_DIR = Paths.get("/tmp/lucene0");
  private static final Path INDEX_DIR1 = Paths.get("/tmp/lucene1");
  private static final Path INDEX_DIR2 = Paths.get("/tmp/lucene2");

  private static List<IndexableField> DOC1 = ImmutableList.of(
      new StringField("name", "tom", Field.Store.NO),
      new StringField("name", "chen", Field.Store.NO),
      new StringField("id", "001", Field.Store.NO),
      new StringField("title", "engineer", Field.Store.NO),
      new StringField("title", "developer", Field.Store.NO),
      new StringField("title", "swe", Field.Store.NO),
      new StringField("gender", "m", Field.Store.NO),
      new NumericDocValuesField("order", 10)
  );

  private static List<IndexableField> DOC2 = ImmutableList.of(
      new StringField("name", "jack", Field.Store.NO),
      new StringField("name", "chen", Field.Store.NO),
      new StringField("id", "002", Field.Store.NO),
      new StringField("title", "artist", Field.Store.NO),
      new StringField("title", "painter", Field.Store.NO),
      new StringField("gender", "m", Field.Store.NO),
      new NumericDocValuesField("order", 20)
  );

  private static List<IndexableField> DOC3 = ImmutableList.of(
      new StringField("name", "jane", Field.Store.NO),
      new StringField("name", "zhang", Field.Store.NO),
      new StringField("id", "003", Field.Store.NO),
      new StringField("title", "administrator", Field.Store.NO),
      new StringField("title", "assistant", Field.Store.NO),
      new StringField("title", "painter", Field.Store.NO),
      new StringField("gender", "f", Field.Store.NO),
      new NumericDocValuesField("order", 30)
  );

  private static List<IndexableField> DOC4 = ImmutableList.of(
      new StringField("name", "mark", Field.Store.NO),
      new StringField("name", "huang", Field.Store.NO),
      new StringField("id", "004", Field.Store.NO),
      new StringField("title", "swe", Field.Store.NO),
      new StringField("gender", "m", Field.Store.NO),
      new NumericDocValuesField("order", 40)
  );

  private List<IndexableField> newDocument(int id) {
    return ImmutableList.of(
        new StringField("uid", Integer.toString(id), Field.Store.NO),
        new NumericDocValuesField("uid", id)
    );
  }

  private Sort newSort(String field) {
    return new Sort(new SortField(field, SortField.Type.LONG));
  }

  private void createIndex(Path indexDir, int uidStart, int uidEnd, boolean withSort) throws IOException {
    var config = new IndexWriterConfig();
    if (withSort) {
      config.setIndexSort(newSort("uid"));
    }
    Directory outputDir = new MMapDirectory(indexDir);
    List<Integer> uids = IntStream.range(uidStart, uidEnd).boxed().collect(Collectors.toList());
    Collections.shuffle(uids);
    try (var writer = new IndexWriter(outputDir, config)) {
      for (int uid: uids) {
        writer.addDocument(newDocument(uid));
      }
      writer.flush();
    }
  }

  @Test
  public void testIndexMerger() throws IOException {
    FileUtils.deleteDirectory(INDEX_DIR1.toFile());
    FileUtils.deleteDirectory(INDEX_DIR2.toFile());
    FileUtils.deleteDirectory(INDEX_DIR.toFile());
    createIndex(INDEX_DIR1, 10, 20, true);
    createIndex(INDEX_DIR2, 20, 30, false);
    boolean useDirectoryMerge = false;
    var config = new IndexWriterConfig();
    config.setIndexSort(newSort("uid"));
    Directory outputDir = new MMapDirectory(INDEX_DIR);
    try (var writer = new IndexWriter(outputDir, config)) {
      if (useDirectoryMerge) {
        MMapDirectory directory1 = new MMapDirectory(INDEX_DIR1);
        MMapDirectory directory2 = new MMapDirectory(INDEX_DIR2);
        writer.addIndexes(directory1, directory2);
      } else {
        DirectoryReader index1 = DirectoryReader.open(new MMapDirectory(INDEX_DIR1));
        DirectoryReader index2 = DirectoryReader.open(new MMapDirectory(INDEX_DIR2));
        LeafReader leafReader1 = index1.leaves().get(0).reader();
        LeafReader leafReader2 = index2.leaves().get(0).reader();
        CodecReader codecReader1 = SlowCodecReaderWrapper.wrap(leafReader1);
        CodecReader codecReader2 = SlowCodecReaderWrapper.wrap(leafReader2);
        writer.addIndexes(codecReader1, codecReader2);
      }

      writer.flush();
      // merge segment
      writer.forceMerge(1);
    }
  }

  @Test
  public void testIndexWriter() throws IOException {
    var config = new IndexWriterConfig();
    config.setIndexSort(newSort("order"));
    Directory outputDir = new MMapDirectory(INDEX_DIR);
    try (var writer = new IndexWriter(outputDir, config)) {
      // writer.deleteDocuments(new Term("id", "001"));
      writer.addDocument(DOC2);
      writer.addDocument(DOC1);
      writer.addDocument(DOC4);
      writer.addDocument(DOC3);
      writer.flush();

      // merge segment
      // writer.forceMerge(1);
    }
  }

  @Test
  public void testIndexReader() throws IOException {
    Directory indexDir = new MMapDirectory(INDEX_DIR);
    try (DirectoryReader reader = DirectoryReader.open(indexDir)) {
      System.out.printf("DirectoryReader: %s\n", reader);
      for (LeafReaderContext ctx: reader.leaves()) {
        LeafReader leafReader = ctx.reader();
        System.out.println("==========================");
        dumpLeafIndex(leafReader, false, true);
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

  private void dumpLeafIndex(LeafReader leafReader, boolean showInvertedFields, boolean showDocValuesFields) throws IOException {
    List<String> invFields = new ArrayList<>();
    List<String> numericDocValuesFields = new ArrayList<>();

    System.out.printf("LeafReader: %s\n", leafReader);
    leafReader.getFieldInfos().forEach((FieldInfo fi) -> {
      System.out.printf("Field: %s, hasPayload: %s, indexOptions: %s, docValuesType: %s\n", fi.name, fi.hasPayloads(),
          fi.getIndexOptions(), fi.getDocValuesType());
      if (fi.getIndexOptions() != IndexOptions.NONE) {
        invFields.add(fi.name);
      }
      if (fi.getDocValuesType() == DocValuesType.NUMERIC) {
        numericDocValuesFields.add(fi.name);
      }
    });

    if (showInvertedFields) {
      for (String field : invFields) {
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

    if (showDocValuesFields) {
      for (String ndvField : numericDocValuesFields) {
        NumericDocValues numericDocValues = leafReader.getNumericDocValues(ndvField);
        for (int i = 0; i < leafReader.maxDoc(); ++i) {
          boolean success = numericDocValues.advanceExact(i);
          assert success;
          System.out.printf("Doc: %d, %s = %d\n", i, ndvField, numericDocValues.longValue());
        }
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
