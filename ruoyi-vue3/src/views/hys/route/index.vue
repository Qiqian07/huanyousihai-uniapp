<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="标题" prop="keyword">
            <el-input v-model="queryParams.keyword" placeholder="线路标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增线路</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-loading="loading" :data="routeList">
         <el-table-column label="编号" prop="routeId" width="80" />
         <el-table-column label="标题" prop="title" min-width="180" show-overflow-tooltip />
         <el-table-column label="天数" prop="days" width="70" align="center" />
         <el-table-column label="普通价(元)" width="110" align="right">
            <template #default="scope">{{ fen2yuan(scope.row.normalPriceCents) }}</template>
         </el-table-column>
         <el-table-column label="VIP价(元)" width="110" align="right">
            <template #default="scope">{{ fen2yuan(scope.row.vipPriceCents) }}</template>
         </el-table-column>
         <el-table-column label="集合时间" prop="gatherTime" width="160" show-overflow-tooltip />
         <el-table-column label="排序" prop="sort" width="70" align="center" />
         <el-table-column label="状态" width="90" align="center">
            <template #default="scope">
               <el-tag :type="scope.row.status === 'ON' ? 'success' : 'info'">{{ scope.row.status === 'ON' ? '上架' : '下架' }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="操作" width="220" fixed="right">
            <template #default="scope">
               <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
               <el-button link :type="scope.row.status === 'ON' ? 'warning' : 'success'" @click="handleStatus(scope.row)">
                  {{ scope.row.status === 'ON' ? '下架' : '上架' }}
               </el-button>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <!-- 编辑弹窗 -->
      <el-dialog :title="form.routeId ? '编辑线路' : '新增线路'" v-model="dialogVisible" width="640px" append-to-body>
         <el-form :model="form" label-width="100px">
            <el-form-item label="标题" required>
               <el-input v-model="form.title" placeholder="线路标题" />
            </el-form-item>
            <el-form-item label="封面图URL">
               <el-input v-model="form.cover" placeholder="https://..." />
            </el-form-item>
            <el-form-item label="视频URL">
               <el-input v-model="form.videoUrl" placeholder="https://..." />
            </el-form-item>
            <el-form-item label="天数" required>
               <el-input-number v-model="form.days" :min="1" :max="60" />
            </el-form-item>
            <el-form-item label="普通价(元)" required>
               <el-input-number v-model="form.normalPrice" :min="0.01" :precision="2" :step="100" />
            </el-form-item>
            <el-form-item label="VIP价(元)" required>
               <el-input-number v-model="form.vipPrice" :min="0.01" :precision="2" :step="100" />
            </el-form-item>
            <el-form-item label="集合时间">
               <el-input v-model="form.gatherTime" placeholder="如：每周六 08:30" />
            </el-form-item>
            <el-form-item label="集合地点">
               <el-input v-model="form.gatherPlace" />
            </el-form-item>
            <el-form-item label="行程简介">
               <el-input v-model="form.itinerary" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item label="排序">
               <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
         </el-form>
         <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitForm">保存</el-button>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="HysRoute">
import { listRoute, addRoute, updateRoute, changeRouteStatus } from '@/api/hys'

const { proxy } = getCurrentInstance()

const routeList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dialogVisible = ref(false)

const data = reactive({
   queryParams: { pageNum: 1, pageSize: 10, keyword: null },
   form: {}
})
const { queryParams, form } = toRefs(data)

function fen2yuan(fen) {
   return fen == null ? '-' : (fen / 100).toFixed(2)
}

function getList() {
   loading.value = true
   listRoute(queryParams.value).then(res => {
      routeList.value = res.rows
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
   form.value = { routeId: null, title: null, cover: null, videoUrl: null, days: 1, normalPrice: null, vipPrice: null, gatherTime: null, gatherPlace: null, itinerary: null, sort: 0 }
}

function handleAdd() {
   reset()
   dialogVisible.value = true
}

function handleUpdate(row) {
   reset()
   form.value = { ...row, normalPrice: row.normalPriceCents / 100, vipPrice: row.vipPriceCents / 100 }
   dialogVisible.value = true
}

function handleStatus(row) {
   const to = row.status === 'ON' ? 'OFF' : 'ON'
   proxy.$modal.confirm(`确认${to === 'ON' ? '上架' : '下架'}线路「${row.title}」？`).then(() => {
      return changeRouteStatus(row.routeId, to)
   }).then(() => {
      proxy.$modal.msgSuccess('操作成功')
      getList()
   }).catch(() => {})
}

function submitForm() {
   if (!form.value.title || !form.value.normalPrice || !form.value.vipPrice) {
      proxy.$modal.msgError('请填写标题与两个价格')
      return
   }
   const payload = {
      routeId: form.value.routeId,
      title: form.value.title,
      cover: form.value.cover,
      videoUrl: form.value.videoUrl,
      days: form.value.days,
      normalPriceCents: Math.round(form.value.normalPrice * 100),
      vipPriceCents: Math.round(form.value.vipPrice * 100),
      gatherTime: form.value.gatherTime,
      gatherPlace: form.value.gatherPlace,
      itinerary: form.value.itinerary,
      sort: form.value.sort
   }
   const call = payload.routeId ? updateRoute(payload) : addRoute(payload)
   call.then(() => {
      proxy.$modal.msgSuccess('保存成功')
      dialogVisible.value = false
      getList()
   })
}

getList()
</script>
