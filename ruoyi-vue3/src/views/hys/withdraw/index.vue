<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
               <el-option label="待审核" value="PENDING" />
               <el-option label="已打款" value="PAID" />
               <el-option label="已驳回" value="REJECTED" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="withdrawList">
         <el-table-column label="流水号" prop="withdrawId" width="90" />
         <el-table-column label="会员" width="150">
            <template #default="scope">{{ scope.row.memberName }}（{{ scope.row.memberPhone }}）</template>
         </el-table-column>
         <el-table-column label="申请金额(元)" width="120" align="right">
            <template #default="scope">{{ fen2yuan(scope.row.amountCents) }}</template>
         </el-table-column>
         <el-table-column label="手续费(元)" width="110" align="right">
            <template #default="scope">{{ fen2yuan(scope.row.feeCents) }}</template>
         </el-table-column>
         <el-table-column label="到账(元)" width="110" align="right">
            <template #default="scope">{{ fen2yuan(scope.row.arriveCents) }}</template>
         </el-table-column>
         <el-table-column label="收款信息" prop="bankInfo" width="180" show-overflow-tooltip />
         <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
               <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="申请时间" prop="createTime" width="160" />
         <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
               <template v-if="scope.row.status === 'PENDING'">
                  <el-button link type="success" icon="Check" @click="handleApprove(scope.row)">打款</el-button>
                  <el-button link type="danger" icon="Close" @click="handleReject(scope.row)">驳回</el-button>
               </template>
               <span v-else>—</span>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <el-dialog title="驳回提现申请" v-model="rejectVisible" width="420px" append-to-body>
         <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请填写驳回原因（可选）" />
         <template #footer>
            <el-button @click="rejectVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmReject">确认驳回</el-button>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="HysWithdraw">
import { listWithdraw, approveWithdraw, rejectWithdraw } from '@/api/hys'

const { proxy } = getCurrentInstance()

const withdrawList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const rejectVisible = ref(false)
const rejectRemark = ref('')
const rejectRow = ref(null)

const data = reactive({
   queryParams: { pageNum: 1, pageSize: 10, status: 'PENDING' }
})
const { queryParams } = toRefs(data)

function fen2yuan(fen) {
   return fen == null ? '-' : (fen / 100).toFixed(2)
}
function statusTag(s) {
   return { PENDING: 'warning', PAID: 'success', REJECTED: 'danger' }[s] || 'info'
}
function statusText(s) {
   return { PENDING: '待审核', PAID: '已打款', REJECTED: '已驳回' }[s] || s
}

function getList() {
   loading.value = true
   listWithdraw(queryParams.value).then(res => {
      withdrawList.value = res.rows
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

function handleApprove(row) {
   proxy.$modal.confirm(`确认已向会员「${row.memberName}」打款 ${fen2yuan(row.arriveCents)} 元（申请 ${fen2yuan(row.amountCents)} 元，手续费 ${fen2yuan(row.feeCents)} 元）？`).then(() => {
      return approveWithdraw(row.withdrawId)
   }).then(() => {
      proxy.$modal.msgSuccess('已标记打款完成')
      getList()
   }).catch(() => {})
}

function handleReject(row) {
   rejectRow.value = row
   rejectRemark.value = ''
   rejectVisible.value = true
}

function confirmReject() {
   rejectWithdraw(rejectRow.value.withdrawId, rejectRemark.value || null).then(() => {
      proxy.$modal.msgSuccess('已驳回，申请金额已退回会员余额')
      rejectVisible.value = false
      getList()
   })
}

getList()
</script>
