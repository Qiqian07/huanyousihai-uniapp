<template>
   <div class="app-container">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
               <el-option label="待审核" value="PENDING" />
               <el-option label="已通过" value="APPROVED" />
               <el-option label="已拒绝" value="REJECTED" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rechargeList">
         <el-table-column label="流水号" prop="rechargeId" width="90" />
         <el-table-column label="会员" width="150">
            <template #default="scope">{{ scope.row.memberName }}（{{ scope.row.memberPhone }}）</template>
         </el-table-column>
         <el-table-column label="金额(元)" width="110" align="right">
            <template #default="scope">{{ fen2yuan(scope.row.amountCents) }}</template>
         </el-table-column>
         <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
               <el-tag :type="statusTag(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
            </template>
         </el-table-column>
         <el-table-column label="申请时间" prop="createTime" width="160" />
         <el-table-column label="审核时间" prop="auditTime" width="160" />
         <el-table-column label="备注" prop="remark" show-overflow-tooltip />
         <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
               <template v-if="scope.row.status === 'PENDING'">
                  <el-button link type="success" icon="Check" @click="handleApprove(scope.row)">通过</el-button>
                  <el-button link type="danger" icon="Close" @click="handleReject(scope.row)">拒绝</el-button>
               </template>
               <span v-else>—</span>
            </template>
         </el-table-column>
      </el-table>

      <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

      <!-- 拒绝备注弹窗 -->
      <el-dialog title="拒绝充值申请" v-model="rejectVisible" width="420px" append-to-body>
         <el-input v-model="rejectRemark" type="textarea" :rows="3" placeholder="请填写拒绝原因（可选）" />
         <template #footer>
            <el-button @click="rejectVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmReject">确认拒绝</el-button>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="HysRecharge">
import { listRecharge, approveRecharge, rejectRecharge } from '@/api/hys'

const { proxy } = getCurrentInstance()

const rechargeList = ref([])
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
   return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }[s] || 'info'
}
function statusText(s) {
   return { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s
}

function getList() {
   loading.value = true
   listRecharge(queryParams.value).then(res => {
      rechargeList.value = res.rows
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
   proxy.$modal.confirm(`确认通过会员「${row.memberName}」的充值申请（${fen2yuan(row.amountCents)} 元）？入账后不可撤销。`).then(() => {
      return approveRecharge(row.rechargeId)
   }).then(() => {
      proxy.$modal.msgSuccess('已通过并入账')
      getList()
   }).catch(() => {})
}

function handleReject(row) {
   rejectRow.value = row
   rejectRemark.value = ''
   rejectVisible.value = true
}

function confirmReject() {
   rejectRecharge(rejectRow.value.rechargeId, rejectRemark.value || null).then(() => {
      proxy.$modal.msgSuccess('已拒绝该充值申请')
      rejectVisible.value = false
      getList()
   })
}

getList()
</script>
