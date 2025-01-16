CLASSPATH = .:Lib/:Lib/jsoup-1.13.1.jar
TESTCLASSPATH = $(CLASSPATH):Test/:Test/junit4.jar
JFLAGS = -g -cp $(TESTCLASSPATH)
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	PageDigest.java \
	Crawler.java \
	Page.java \
	PageSet.java \
	InvertedIndex.java \
	TextCleaner.java \
	Lib/PorterStemmer.java \
	Search.java

default: classes

classes: $(CLASSES:.java=.class)

search: classes
	java -cp $(CLASSPATH) Search

crawl: classes
	java -cp $(CLASSPATH) Crawler

clean:
	rm -f *.class Test/*.class

test-build: classes Test/TextCleanerTest.class Test/PageSetTest.class \
	Test/InvertedIndexTest.class Test/CrawlTest.class Test/SearchTest.class \
	Test/TestRunner.class

## Text Cleaner Tests

test-text-cleaner-lowercasing: test-build
	java -cp $(TESTCLASSPATH) TestRunner TextCleanerTest#testLowercasing

test-text-cleaner-nonalpha: test-build
	java -cp $(TESTCLASSPATH) TestRunner TextCleanerTest#testNonAlpha

test-text-cleaner-stopwords: test-build
		java -cp $(TESTCLASSPATH) TestRunner TextCleanerTest#testStopwords

test-text-cleaner: test-build
	java -cp $(TESTCLASSPATH) TestRunner TextCleanerTest

# Test Page Set
test-page-set-add: test-build
	java -cp $(TESTCLASSPATH) TestRunner PageSetTest#testAdd

test-page-set-contains: test-build
	java -cp $(TESTCLASSPATH) TestRunner PageSetTest#testContains

test-page-set-intersect: test-build
	java -cp $(TESTCLASSPATH) TestRunner PageSetTest#testIntersect

test-page-set-iterator: test-build
	java -cp $(TESTCLASSPATH) TestRunner PageSetTest#testIterator

test-page-set: test-build
	java -cp $(TESTCLASSPATH) TestRunner PageSetTest

# Test Inverted Index
test-inverted-index-add: test-build
	java -cp $(TESTCLASSPATH) TestRunner InvertedIndexTest#testAdd

test-inverted-index-lookup-notpresent: test-build
	java -cp $(TESTCLASSPATH) TestRunner InvertedIndexTest#testLookupNotPresent

test-inverted-index-lookup-singlepage: test-build
	java -cp $(TESTCLASSPATH) TestRunner InvertedIndexTest#testLookupSinglePage

test-inverted-index-lookup-multipage: test-build
	java -cp $(TESTCLASSPATH) TestRunner InvertedIndexTest#testLookupMultiPage

test-inverted-index: test-build
	java -cp $(TESTCLASSPATH) TestRunner InvertedIndexTest

# Crawler tests
test-crawl-limited: test-build
	java -cp $(TESTCLASSPATH) TestRunner CrawlTest#testLimitedCrawl

test-crawl-deeper: test-build
	java -cp $(TESTCLASSPATH) TestRunner CrawlTest#testDeeperCrawl

test-crawl-write: test-build
	java -cp $(TESTCLASSPATH) TestRunner CrawlTest#testWriteSerialization

test-crawl: test-build
	java -cp $(TESTCLASSPATH) TestRunner CrawlTest

# Search tests
test-search-read: test-build
	java -cp $(TESTCLASSPATH) TestRunner SearchTest#testReadSerialization

test-search-lookup-single: test-build
	java -cp $(TESTCLASSPATH) TestRunner SearchTest#testLookupSingleWord

test-search-lookup-multi: test-build
	java -cp $(TESTCLASSPATH) TestRunner SearchTest#testLookupMultiWordsDeeper

test-search: test-build
	java -cp $(TESTCLASSPATH) TestRunner SearchTest

test: test-text-cleaner test-page-set test-inverted-index test-crawl test-search
