import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useArticleStore = defineStore('article', () => {
  // 状态
  const articles = ref([])
  const currentArticle = ref(null)
  const categories = ref([])
  const tags = ref([])
  
  // 筛选和排序状态（持久化到localStorage）
  const filterCategory = ref(localStorage.getItem('filterCategory') || '')
  const sortBy = ref(localStorage.getItem('sortBy') || 'latest')
  
  // 分页状态
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  
  // 方法
  function setArticles(list) {
    articles.value = list
  }
  
  function setCurrentArticle(article) {
    currentArticle.value = article
  }
  
  function setCategories(list) {
    categories.value = list
  }
  
  function setTags(list) {
    tags.value = list
  }
  
  function setFilterCategory(category) {
    filterCategory.value = category
    localStorage.setItem('filterCategory', category)
  }
  
  function setSortBy(sort) {
    sortBy.value = sort
    localStorage.setItem('sortBy', sort)
  }
  
  function setPagination(page, size, totalCount) {
    currentPage.value = page
    pageSize.value = size
    total.value = totalCount
  }
  
  function clearFilters() {
    filterCategory.value = ''
    sortBy.value = 'latest'
    localStorage.removeItem('filterCategory')
    localStorage.setItem('sortBy', 'latest')
  }
  
  return {
    articles,
    currentArticle,
    categories,
    tags,
    filterCategory,
    sortBy,
    currentPage,
    pageSize,
    total,
    setArticles,
    setCurrentArticle,
    setCategories,
    setTags,
    setFilterCategory,
    setSortBy,
    setPagination,
    clearFilters
  }
})
