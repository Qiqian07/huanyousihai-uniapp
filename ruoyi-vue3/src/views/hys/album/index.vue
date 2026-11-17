<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="批次" prop="keyword">
            <el-input v-model="queryParams.keyword" placeholder="批次标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新建相册批次</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-loading="loading" :data="albumList">
         <el-table-column label="编号" prop="albumId" width="80" />
         <el-table-column label="批次标题" prop="batchTitle" min-width="180" show-overflow-tooltip />
         <el-table-column label="出行日期" prop="tripDate" width="120" />
         <el-table-column label="封面" width="100">
            <template #default="scope">
               <el-image v-if="scope.row.cover" :src="scope.row.cover" :preview-src-list="[scope.row.cover]" preview-teleported fit="cover" style="width: 60px; height: 40px" />
               <span v-else>—</span>
            </template>
         </el-table-column>
         <el-table-column label="图片数" width="90" align="center">
            <template #default="scope">{{ imageCount(scope.row.images) }}</template>
         </el-table-column>
         <el-table-column label="创建时间" prop="createTime" width="160" />
         <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog :title="form.albumId ? '编辑相册批次' : '新建相册批次'" v-model="dialogVisible" width="640px" append-to-body>
         <el-form :model="form" label-width="100px">
            <el-form-item label="批次标题" required>
               <el-input v-model="form.batchTitle" placeholder="如：2026 春季滇西北 7 日团" />
            </el-form-item>
            <el-form-item label="出行日期">
               <el-date-picker v-model="form.tripDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 200px" />
            </el-form-item>
            <el-form-item label="封面URL">
               <el-input v-model="form.cover" />
            </el-form-item>
            <el-form-item label="图片URL列表" required>
               <el-input v-model="form.images" type="textarea" :rows="6" placeholder="每行一个图片 URL" />
            </el-form-item>
         </el-form>
         <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitForm">保存</el-button>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="HysAlbum">
import { listAlbum, addAlbum, updateAlbum } from '@/api/hys'

const { proxy } = getCurrentInstance()

const albumList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dialogVisible = ref(false)

const data = reactive({
   queryParams: { pageNum: 1, pageSize: 10, keyword: null },
   form: {}
})
const { queryParams, form } = toRefs(data)

function imageCount(images) {
   if (!images) return 0
   try {
      const arr = JSON.parse(images)
      return Array.isArray(arr) ? arr.length : 0
   } catch (e) {
      return 0
   }
}

function getList() {
   loading.value = true
   listAlbum(queryParams.value).then(res => {
      albumList.value = res.rows
      total.value = res.total
      loading.value = false
   })
}

function handleQuery() {
   queryParams.value.pageNum = 1
   getList()
}

function resetQuery() {
   proxy.resetForm('queryRef')
   handleQuery()
}

function reset() {
   form.value = { albumId: null, batchTitle: null, tripDate: null, cover: null, images: '' }
}

function handleAdd() {
   reset()
   dialogVisible.value = true
}

function handleUpdate(row) {
   reset()
   /* 兼容两种存储格式：URL 数组（后台录入）与分类对象 {hotel,food,scenery}（种子数据） */
   let urls = []
   try {
      const parsed = JSON.parse(row.images || '[]')
      if (Array.isArray(parsed)) urls = parsed
      else urls = [].concat(parsed.hotel || [], parsed.food || [], parsed.scenery || [])
   } catch (e) { /* 忽略脏数据 */ }
   form.value = { ...row, images: Array.isArray(urls) ? urls.join('\n') : '' }
   dialogVisible.value = true
}

function submitForm() {
   if (!form.value.batchTitle) {
      proxy.$modal.msgError('请填写批次标题')
      return
   }
   const urls = (form.value.images || '').split('\n').map(s => s.trim()).filter(Boolean)
   if (!urls.length) {
      proxy.$modal.msgError('请至少填写一张图片 URL')
      return
   }
   const payload = {
      albumId: form.value.albumId,
      batchTitle: form.value.batchTitle,
      tripDate: form.value.tripDate,
      cover: form.value.cover,
      images: JSON.stringify(urls)
   }
   const call = payload.albumId ? updateAlbum(payload) : addAlbum(payload)
   call.then(() => {
      proxy.$modal.msgSuccess('保存成功')
      dialogVisible.value = false
      getList()
   })
}

getList()
</script>
