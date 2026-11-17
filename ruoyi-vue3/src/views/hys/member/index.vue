<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="关键字" prop="keyword">
            <el-input v-model="queryParams.keyword" placeholder="姓名 / 手机号" clearable style="width: 200px" @keyup.enter="handleQuery" />
         </el-form-item>
         <el-form-item label="等级" prop="level">
            <el-select v-model="queryParams.level" placeholder="全部" clearable style="width: 140px">
               <el-option label="普通会员" value="NORMAL" />
               <el-option label="VIP" value="VIP" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-loading="loading" :data="memberList">
         <el-table-column label="ID" prop="memberId" width="70" />
         <el-table-column label="姓名" prop="name" width="120" show-overflow-tooltip />
         <el-table-column label="手机号" prop="phone" width="130" />
         <el-table-column label="等级" width="100" align="center">
            <template #default="scope">
               <el-tag :type="scope.row.level === 'VIP' ? 'warning' : 'info'">{{ scope.row.level === 'VIP' ? 'VIP' : '普通' }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="推荐人" prop="referrerName" width="120" />
         <el-table-column label="直推人数" prop="directCount" width="90" align="center" />
         <el-table-column label="团队人数" prop="teamCount" width="90" align="center" />
         <el-table-column label="余额(元)" width="110" align="right">
            <template #default="scope">{{ fen2yuan(scope.row.balanceCents) }}</template>
         </el-table-column>
         <el-table-column label="绿积分" prop="greenPoints" width="90" align="center" />
         <el-table-column label="VIP状态" prop="vipState" width="110" align="center" />
         <el-table-column label="注册时间" prop="createTime" width="160" />
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
   </div>
</template>

<script setup name="HysMember">
import { listMember } from '@/api/hys'

const { proxy } = getCurrentInstance()

const memberList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
   queryParams: { pageNum: 1, pageSize: 10, keyword: null, level: null }
})
const { queryParams } = toRefs(data)

function fen2yuan(fen) {
   return fen == null ? '-' : (fen / 100).toFixed(2)
}

function getList() {
   loading.value = true
   listMember(queryParams.value).then(res => {
      memberList.value = res.rows
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

getList()
</script>
