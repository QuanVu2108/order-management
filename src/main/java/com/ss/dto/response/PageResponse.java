package com.ss.dto.response;

import java.util.List;

public class PageResponse<T> {
    private long count;
    private List<T> rows;
    private int page;
    private int limit;

    public long getTotalPage() {
        long mod = this.count % (long)this.limit;
        long totalPage = this.count / (long)this.limit;
        totalPage += mod > 0L ? 1L : 0L;
        return totalPage;
    }

    public static <T> PageResponseBuilder<T> builder() {
        return new PageResponseBuilder();
    }

    public long getCount() {
        return this.count;
    }

    public List<T> getRows() {
        return this.rows;
    }

    public int getPage() {
        return this.page;
    }

    public int getLimit() {
        return this.limit;
    }

    public void setCount(final long count) {
        this.count = count;
    }

    public void setRows(final List<T> rows) {
        this.rows = rows;
    }

    public void setPage(final int page) {
        this.page = page;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof PageResponse)) {
            return false;
        } else {
            PageResponse<?> other = (PageResponse)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getCount() != other.getCount()) {
                return false;
            } else if (this.getPage() != other.getPage()) {
                return false;
            } else if (this.getLimit() != other.getLimit()) {
                return false;
            } else {
                Object this$rows = this.getRows();
                Object other$rows = other.getRows();
                if (this$rows == null) {
                    if (other$rows != null) {
                        return false;
                    }
                } else if (!this$rows.equals(other$rows)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PageResponse;
    }

    public String toString() {
        long var10000 = this.getCount();
        return "PageResponse(count=" + var10000 + ", rows=" + this.getRows() + ", page=" + this.getPage() + ", limit=" + this.getLimit() + ")";
    }

    public PageResponse() {
    }

    public PageResponse(final long count, final List<T> rows, final int page, final int limit) {
        this.count = count;
        this.rows = rows;
        this.page = page;
        this.limit = limit;
    }

    public static class PageResponseBuilder<T> {
        private long count;
        private List<T> rows;
        private int page;
        private int limit;

        PageResponseBuilder() {
        }

        public PageResponseBuilder<T> count(final long count) {
            this.count = count;
            return this;
        }

        public PageResponseBuilder<T> rows(final List<T> rows) {
            this.rows = rows;
            return this;
        }

        public PageResponseBuilder<T> page(final int page) {
            this.page = page;
            return this;
        }

        public PageResponseBuilder<T> limit(final int limit) {
            this.limit = limit;
            return this;
        }

        public PageResponse<T> build() {
            return new PageResponse(this.count, this.rows, this.page, this.limit);
        }

        public String toString() {
            return "PageResponse.PageResponseBuilder(count=" + this.count + ", rows=" + this.rows + ", page=" + this.page + ", limit=" + this.limit + ")";
        }
    }
}
