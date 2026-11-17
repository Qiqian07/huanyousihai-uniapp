<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
               <el-option label="待处理" value="PENDING" />
               <el-option label="已修复" value="REPAIRED" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-alert type="warning" :closable="false" show-icon class="mb8"
         title="异常对账：计佣中断的订单（推荐关系缺失）。补绑推荐人后系统将按最新关系链重算佣金，请谨慎操作。" />

      <el-table v-loading="loading" :data="gapList">
         <el-table-column label="缺口编号" prop="gapId" width="90" />
         <el-table-column label="订单号" prop="orderId" width="170" show-overflow-tooltip />
         <el-table-column label="新会员" width="150">
            <template #default="scope">{{ scope.row.memberName }}（{{ scope.row.memberPhone }}）</template>
         </el-table-column>
         <el-table-column label="中断原因" prop="reason" width="200" show-overflow-tooltip />
         <el-table-column label="涉及金额(元)" width="120" align="right">
            <template #default="scope">{{ fen2yuan(scope.row.amountCents) }}</template>
         </el-table-column>
         <el-table-column label="说明" prop="note" min-width="180" show-overflow-tooltip />
         <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
               <el-tag :type="scope.row.status === 'PENDING' ? 'danger' : 'success'">
                  {{ scope.row.status === 'PENDING' ? '待处理' : '已修复' }}
               </el-tag>
            </template>
         </el-table-column>
         <el-table-column label="修复时间" prop="repairedTime" width="160" />
         <el-table-column label="操作" width="130" fixed="right">
            <template #default="scope">
               <el-button v-if="scope.row.status === 'PENDING'" link type="primary" icon="Link" @click="handleRepair(scope.row)">补绑重算</el-button>
               <span v-else>—</span>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog title="补绑推荐人并重算佣金" v-model="repairVisible" width="460px" append-to-body>
         <el-form label-width="110px">
            <el-form-item label="订单号">
               <span>{{ repairRow && repairRow.orderId }}</span>
            </el-form-item>
            <el-form-item label="新推荐人" required>
               <el-input v-model="repairPhone" placeholder="请输入推荐人手机号" style="width: 260px" />
            </el-form-item>
         </el-form>
         <el-alert type="error" :closable="false" show-icon
            title="补绑后立即按新关系链重算该订单佣金，操作不可撤销，请二次确认手机号无误。" />
         <template #footer>
            <el-button @click="repairVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmRepair">确认补绑并重算</el-button>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="HysRecon">
import { listRecon, repairRecon } from '@/api/hys'

const { proxy } = getCurrentInstance()

const gapList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const repairVisible = ref(false)
const repairRow = ref(null)
const repairPhone = ref('')

const data = reactive({
   queryParams: { pageNum: 1, pageSize: 10, status: 'PENDING' }
})
const { queryParams } = toRefs(data)

function fen2yuan(fen) {
   return fen == null ? '-' : (fen / 100).toFixed(2)
}

function getList() {
   loading.value = true
   listRecon(queryParams.value).then(res => {
      gapList.value = res.rows
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

function handleRepair(row) {
   repairRow.value = row
   repairPhone.value = ''
   repairVisible.value = true
}

function confirmRepair() {
   if (!repairPhone.value) {
      proxy.$modal.msgError('请输入推荐人手机号')
      return
   }
   proxy.$modal.confirm(`确认为订单「${repairRow.value.orderId}」补绑推荐人 ${repairPhone.value} 并立即重算佣金？`).then(() => {
      return repairRecon(repairRow.value.gapId, repairPhone.value)
   }).then(() => {
      proxy.$modal.msgSuccess('补绑重算完成')
      repairVisible.value = false
      getList()
   }).catch(() => {})
}

getList()
</script>
