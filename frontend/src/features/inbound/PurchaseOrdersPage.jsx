import ExportButton from "../../components/ExportButton";
import PageHeader from "../../components/PageHeader";
import { columnSortOrder } from "../../utils/sort";
import RowLink from "../../components/RowLink";
import FitTable from "../../components/FitTable";
import { useMemo } from "react";
import { useProducts } from "../../hooks/useProducts";
import { useRecordView } from "../../hooks/useRecordView";
import { useListParams } from "../../hooks/useListParams";
import { useNavigate, useLocation } from "react-router-dom";
import {
  keepPreviousData,
  useQuery,
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";
import {
  Card,
  Button,
  Input,
  Form,
  Select,
  DatePicker,
  InputNumber,
  Row,
  Col,
  Table,
  Space,
  Tag,
  Descriptions,
  Empty,
  Popconfirm,
  Divider,
  App as AntdApp,
} from "antd";
import {
  PlusOutlined,
  SearchOutlined,
  DeleteOutlined,
  SendOutlined,
  ReloadOutlined,
  CheckOutlined,
  CloseOutlined,
  InboxOutlined,
  ArrowLeftOutlined,
  EditOutlined,
} from "@ant-design/icons";
import dayjs from "dayjs";
import Can from "../../components/Can";
import { useAuth } from "../../auth/AuthContext";
import { getErrorMessage } from "../../api/client";
import { handleFormError } from "../../utils/formErrors";
import { P } from "../../constants/permissions";
import { purchaseOrdersApi } from "../../api/purchaseOrders.api";
import { suppliersApi } from "../../api/partners.api";
import { warehousesApi } from "../../api/warehouses.api";

const PO_STATUS = {
  DRAFT: { color: "default", label: "Nháp" },
  PENDING_REVIEW: { color: "gold", label: "Chờ kiểm tra" },
  PENDING_APPROVAL: { color: "blue", label: "Chờ phê duyệt" },
  APPROVED: { color: "green", label: "Đã duyệt" },
  ORDERED: { color: "geekblue", label: "Đã đặt" },
  CLOSED: { color: "default", label: "Đã đóng" },
  REJECTED: { color: "red", label: "Bị từ chối" },
  CANCELLED: { color: "red", label: "Đã huỷ" },
};
const poTag = (s) => (
  <Tag color={PO_STATUS[s]?.color || "default"}>{PO_STATUS[s]?.label || s}</Tag>
);
const PO_STATUS_OPTS = Object.entries(PO_STATUS).map(([value, m]) => ({
  value,
  label: m.label,
}));

// Map id -> tên NCC / kho (nhẹ, dùng cho bảng list + header chi tiết)
function useNameMaps() {
  const suppliers = useQuery({
    queryKey: ["suppliers"],
    queryFn: suppliersApi.list,
  });
  const warehouses = useQuery({
    queryKey: ["warehouses", "active"],
    queryFn: () => warehousesApi.list(false),
  });
  const supplierMap = useMemo(
    () => Object.fromEntries((suppliers.data || []).map((s) => [s.id, s])),
    [suppliers.data],
  );
  const warehouseMap = useMemo(
    () => Object.fromEntries((warehouses.data || []).map((w) => [w.id, w])),
    [warehouses.data],
  );
  return { suppliers, warehouses, supplierMap, warehouseMap };
}

export default function PurchaseOrdersPage() {
  // Chế độ xem nằm ở URL: /purchase-orders | /purchase-orders/new | /purchase-orders/<id>
  const { mode, id, openList, openCreate, openDetail } =
    useRecordView("/purchase-orders");

  return (
    <div>
      {mode !== "list" && (
        <PageHeader
          title="Đơn mua hàng (PO)"
          onBack={
            <Button icon={<ArrowLeftOutlined />} onClick={openList}>
              Danh sách
            </Button>
          }
        />
      )}

      {mode === "list" && <POList onOpen={openDetail} onCreate={openCreate} />}
      {mode === "create" && (
        <CreatePO onCreated={(r) => openDetail(r.id, { replace: true })} />
      )}
      {mode === "detail" && id && <PODetail id={id} />}
    </div>
  );
}

// ---- Bảng danh sách ----
function POList({ onOpen, onCreate }) {
  // Bộ lọc nằm trong query string (?q=&status=&page=&size=&sort=&dir=).
  // Thay cho useState + location.state: F5 không mất bộ lọc, gửi link được,
  // Back lùi đúng bộ lọc trước, và Dashboard chỉ cần trỏ tới ?status=... .
  const {
    keyword,
    status,
    page,
    size,
    sort,
    dir,
    sorter,
    setKeyword,
    setStatus,
    setPager,
    setSorter,
  } = useListParams();
  const { supplierMap, warehouseMap } = useNameMaps();

  const list = useQuery({
    queryKey: ["po-list", keyword, status, page, size, sort, dir],
    queryFn: () =>
      purchaseOrdersApi.list({
        keyword,
        status,
        page: page,
        size: size,
        sort,
        dir,
      }),
    placeholderData: keepPreviousData,
  });
  const pageData = list.data;

  const columns = [
    {
      title: "Mã đơn",
      dataIndex: "poNumber",
      sorter: true,
      sortOrder: columnSortOrder(sorter, "poNumber"),
      render: (v, r) => (
        <RowLink onClick={() => onOpen(r.id)}>{v || r.id}</RowLink>
      ),
    },
    {
      title: "Trạng thái",
      dataIndex: "status",
      sorter: true,
      sortOrder: columnSortOrder(sorter, "status"),
      width: 140,
      render: poTag,
    },
    {
      title: "Nhà cung cấp",
      dataIndex: "supplierId",
      render: (v) => supplierMap[v]?.name || v,
    },
    {
      title: "Kho",
      dataIndex: "warehouseId",
      render: (v) => warehouseMap[v]?.name || v,
      width: 150,
    },
    {
      title: "Ngày dự kiến",
      dataIndex: "expectedDate",
      sorter: true,
      sortOrder: columnSortOrder(sorter, "expectedDate"),
      width: 120,
      render: (v) => (v ? dayjs(v).format("DD/MM/YYYY") : "—"),
    },
    { title: "Người tạo", dataIndex: "createdBy", width: 130 },
    {
      title: "Tạo lúc",
      dataIndex: "createdAt",
      sorter: true,
      sortOrder: columnSortOrder(sorter, "createdAt"),
      width: 150,
      render: (v) => (v ? dayjs(v).format("DD/MM/YYYY HH:mm") : "—"),
    },
  ];

  return (
    <>
      <PageHeader
        title="Đơn mua hàng (PO)"
        extra={
          <>
            <ExportButton
              filename="don-mua-hang.xlsx"
              fetchRows={() =>
                purchaseOrdersApi
                  .list({ keyword, status, sort, dir, size: 10000 })
                  .then((r) => r.content)
              }
            />
            <Input.Search
              allowClear
              key={`q-${keyword}`}
              defaultValue={keyword}
              placeholder="Tìm theo mã đơn"
              style={{ width: 220 }}
              prefix={<SearchOutlined />}
              onSearch={(v) => setKeyword(v)}
            />
            <Select
              allowClear
              placeholder="Lọc trạng thái"
              style={{ width: 180 }}
              options={PO_STATUS_OPTS}
              value={status}
              onChange={(v) => setStatus(v)}
            />
            <Button
              icon={<ReloadOutlined />}
              onClick={() => list.refetch()}
              loading={list.isFetching}
            />
            <Can permission={P.INBOUND_CREATE_PO}>
              <Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>
                Tạo đơn mua
              </Button>
            </Can>
          </>
        }
      />

      <FitTable
        rowKey="id"
        loading={list.isLoading}
        dataSource={pageData?.content || []}
        columns={columns}
        scroll={{ x: "max-content" }}
        onChange={(_p, _f, s, extra) => {
          if (extra.action === "sort") setSorter(s);
        }}
        pagination={{
          current: (pageData?.page ?? 0) + 1,
          pageSize: pageData?.size ?? 20,
          total: pageData?.totalElements ?? 0,
          showSizeChanger: true,
          showTotal: (t) => `Tổng ${t}`,
          onChange: (p, s) => setPager(p - 1, s),
        }}
      />
    </>
  );
}

// ---- Lookups cho form tạo & chi tiết (kèm sản phẩm) ----
function useLookups() {
  const { suppliers, warehouses, supplierMap, warehouseMap } = useNameMaps();
  const { query: products, list: productList, map: productMap } = useProducts();
  return {
    suppliers,
    warehouses,
    products,
    productList,
    productMap,
    supplierMap,
    warehouseMap,
  };
}
const productOptions = (list) =>
  list.map((p) => ({ value: p.id, label: `${p.name} · ${p.sku}` }));

// ---- Form tạo / sửa PO ----
// Khi điều hướng kèm state { editPo } (từ nút "Sửa" ở màn chi tiết) thì form
// chạy ở chế độ SỬA: prefill toàn bộ dữ liệu đơn và submit gọi API update.
function CreatePO({ onCreated }) {
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm();
  const location = useLocation();
  const editPo = location.state?.editPo || null;
  const isEdit = !!editPo;
  // [Nhu cầu -> Tạo PO] điền sẵn (kho/dòng hàng) nhưng vẫn là TẠO MỚI (không phải sửa).
  const prefill = location.state?.prefill || null;
  const { suppliers, warehouses, productList } = useLookups();
  const { user } = useAuth();
  // Đơn giá tự điền theo giá vốn (costPrice); khoá mặc định, chỉ ADMIN sửa tay.
  const canEditPrice = user?.role === "ADMIN";
  const pById = Object.fromEntries((productList || []).map((p) => [p.id, p]));
  const fillPriceFromProduct = (name, productId) => {
    const p = pById[productId];
    form.setFieldValue(
      ["lines", name, "unitPrice"],
      p?.costPrice != null ? Number(p.costPrice) : 0,
    );
  };

  // Giá trị khởi tạo: chế độ sửa nạp lại đơn; chế độ điền-sẵn nạp kho + dòng từ nhu cầu.
  const initialValues = isEdit
    ? {
        warehouseId: editPo.warehouseId,
        expectedDate: editPo.expectedDate ? dayjs(editPo.expectedDate) : null,
        lines: (editPo.details || []).map((d) => ({
          productId: d.productId,
          supplierId: d.supplierId,
          quantityOrdered: d.quantityOrdered,
          unitPrice: d.unitPrice != null ? Number(d.unitPrice) : undefined,
        })),
      }
    : prefill
    ? {
        warehouseId: prefill.warehouseId,
        lines: (prefill.lines || [{}]).map((l) => ({
          productId: l.productId,
          supplierId: l.supplierId,
          quantityOrdered: l.quantityOrdered,
          // đơn giá gợi ý theo giá vốn (nếu đã tải được sản phẩm); vẫn sửa được.
          unitPrice: pById[l.productId]?.costPrice != null ? Number(pById[l.productId].costPrice) : undefined,
        })),
      }
    : { lines: [{}] };

  const qc = useQueryClient();
  const saveMut = useMutation({
    mutationFn: (body) =>
      isEdit
        ? purchaseOrdersApi.update(editPo.id, body)
        : purchaseOrdersApi.create(body),
    onSuccess: (po) => {
      // Ghi thẳng dữ liệu vừa lưu vào cache để màn chi tiết hiện NGAY (không cần F5):
      // response update/create đã kèm details (giá/NCC mới). Không có dòng này thì
      // useQuery(['po', id]) trả bản cache cũ trước khi refetch → thấy giá cũ.
      qc.setQueryData(["po", po.id], po);
      qc.invalidateQueries({ queryKey: ["po-list"] });
      message.success(
        isEdit
          ? `Đã cập nhật đơn ${po.poNumber || ""}`.trim()
          : `Đã tạo đơn ${po.poNumber || ""}`.trim(),
      );
      onCreated(po);
    },
    onError: (e) => handleFormError(form, e, message),
  });

  const submit = async () => {
    const v = await form.validateFields();
    saveMut.mutate({
      warehouseId: v.warehouseId,
      expectedDate: v.expectedDate ? v.expectedDate.format("YYYY-MM-DD") : null,
      lines: v.lines.map((l) => ({
        productId: l.productId,
        supplierId: l.supplierId,
        quantityOrdered: l.quantityOrdered,
        unitPrice: l.unitPrice ?? null,
      })),
    });
  };

  return (
    <Card
      title={
        isEdit ? `Sửa đơn mua ${editPo.poNumber || editPo.id}` : "Tạo đơn mua mới"
      }
    >
      <Form form={form} layout="vertical" initialValues={initialValues}>
        <Row gutter={16}>
          <Col xs={24} md={12}>
            <Form.Item
              name="warehouseId"
              label="Kho nhận"
              rules={[{ required: true, message: "Chọn kho" }]}
            >
              <Select
                showSearch
                optionFilterProp="label"
                loading={warehouses.isLoading}
                placeholder="Chọn kho"
                options={(warehouses.data || []).map((w) => ({
                  value: w.id,
                  label: `${w.name} (${w.code})`,
                }))}
              />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item name="expectedDate" label="Ngày dự kiến"
              rules={[{
                validator: (_, v) => (!v || v.isAfter(dayjs(), 'day'))
                  ? Promise.resolve()
                  : Promise.reject(new Error('Ngày dự kiến phải sau ngày hiện tại')),
              }]}>
              <DatePicker style={{ width: "100%" }} format="DD/MM/YYYY"
                disabledDate={(d) => d && d <= dayjs().endOf('day')} />
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left" style={{ margin: "4px 0 12px" }}>
          Dòng hàng
        </Divider>
        <Form.List name="lines">
          {(fields, { add, remove }) => (
            <>
              {fields.map(({ key, name, ...rest }) => (
                <Row gutter={8} key={key} align="middle">
                  <Col flex="auto">
                    <Form.Item
                      {...rest}
                      name={[name, "productId"]}
                      rules={[{ required: true, message: "Chọn sản phẩm" }]}
                    >
                      <Select
                        showSearch
                        optionFilterProp="label"
                        placeholder="Sản phẩm"
                        options={productOptions(productList)}
                        onChange={(pid) => fillPriceFromProduct(name, pid)}
                      />
                    </Form.Item>
                  </Col>
                  <Col flex="200px">
                    <Form.Item
                      {...rest}
                      name={[name, "supplierId"]}
                      rules={[{ required: true, message: "Chọn NCC" }]}
                    >
                      <Select
                        showSearch
                        optionFilterProp="label"
                        placeholder="Nhà cung cấp"
                        loading={suppliers.isLoading}
                        options={(suppliers.data || []).map((s) => ({
                          value: s.id,
                          label: `${s.name} (${s.code})`,
                        }))}
                      />
                    </Form.Item>
                  </Col>
                  <Col flex="130px">
                    <Form.Item
                      {...rest}
                      name={[name, "quantityOrdered"]}
                      rules={[{ required: true, message: "SL" }]}
                    >
                      <InputNumber
                        min={1}
                        placeholder="SL đặt"
                        style={{ width: "100%" }}
                      />
                    </Form.Item>
                  </Col>
                  <Col flex="150px">
                    <Form.Item
                      {...rest}
                      name={[name, "unitPrice"]}
                      tooltip={
                        canEditPrice
                          ? "Tự điền theo giá vốn, có thể sửa"
                          : "Giá vốn — chỉ ADMIN sửa được"
                      }
                    >
                      <InputNumber
                        min={0}
                        placeholder="Đơn giá"
                        style={{ width: "100%" }}
                        disabled={!canEditPrice}
                        formatter={fmt}
                        parser={parse}
                      />
                    </Form.Item>
                  </Col>
                  <Col flex="40px">
                    <Button
                      danger
                      type="text"
                      icon={<DeleteOutlined />}
                      disabled={fields.length === 1}
                      onClick={() => remove(name)}
                    />
                  </Col>
                </Row>
              ))}
              <Button
                type="dashed"
                block
                icon={<PlusOutlined />}
                onClick={() => add({})}
              >
                Thêm dòng
              </Button>
            </>
          )}
        </Form.List>

        <div style={{ marginTop: 16, textAlign: "right" }}>
          <Button type="primary" onClick={submit} loading={saveMut.isPending}>
            {isEdit ? "Lưu thay đổi" : "Tạo đơn"}
          </Button>
        </div>
      </Form>
    </Card>
  );
}

// ---- Chi tiết PO + workflow ----
function PODetail({ id }) {
  const { message } = AntdApp.useApp();
  const { hasPermission, user } = useAuth();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const { productMap, supplierMap, warehouseMap } = useLookups();

  const {
    data: po,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["po", id],
    queryFn: () => purchaseOrdersApi.get(id),
  });

  const mkAct = (mutFn, okMsg) => ({
    mutationFn: () => mutFn(id),
    onSuccess: (updated) => {
      message.success(okMsg);
      qc.setQueryData(["po", id], updated);
      qc.invalidateQueries({ queryKey: ["po-list"] });
      // Duyệt/từ chối PO xoá thông báo "PO chờ duyệt" ở BE -> làm mới chuông ngay.
      qc.invalidateQueries({ queryKey: ["notifications"] });
      qc.invalidateQueries({ queryKey: ["notif-unread"] });
    },
    onError: (e) => message.error(getErrorMessage(e)),
  });
  const reviewMut = useMutation(
    mkAct(purchaseOrdersApi.submitReview, "Đã gửi kiểm tra"),
  );
  const approvalMut = useMutation(
    mkAct(purchaseOrdersApi.submitApproval, "Đã trình phê duyệt"),
  );
  const approveMut = useMutation(
    mkAct(purchaseOrdersApi.approve, "Đã phê duyệt"),
  );
  const rejectMut = useMutation(mkAct(purchaseOrdersApi.reject, "Đã từ chối"));

  if (isLoading) return <Card loading />;
  if (isError)
    return (
      <Card>
        <Empty description={getErrorMessage(error, "Không tìm thấy đơn mua")} />
      </Card>
    );

  const s = po.status;
  const columns = [
    {
      title: "Sản phẩm",
      dataIndex: "productId",
      render: (pid) => productMap[pid]?.name || pid,
    },
    {
      title: "Nhà cung cấp",
      dataIndex: "supplierId",
      width: 160,
      ellipsis: true,
      render: (v) => supplierMap[v]?.name || "—",
    },
    {
      title: "SKU",
      dataIndex: "productId",
      render: (pid) => productMap[pid]?.sku || "—",
      width: 120,
    },
    {
      title: "SL đặt",
      dataIndex: "quantityOrdered",
      width: 90,
      align: "right",
    },
    {
      title: "SL đã nhận",
      dataIndex: "quantityReceived",
      width: 110,
      align: "right",
    },
    {
      title: "Đơn giá",
      dataIndex: "unitPrice",
      width: 120,
      align: "right",
      render: (v) => (v != null ? Number(v).toLocaleString("vi-VN") : "—"),
    },
    {
      title: "Thành tiền",
      key: "lt",
      width: 130,
      align: "right",
      render: (_, r) =>
        r.unitPrice != null
          ? (Number(r.unitPrice) * r.quantityOrdered).toLocaleString("vi-VN")
          : "—",
    },
  ];
  const busy =
    reviewMut.isPending ||
    approvalMut.isPending ||
    approveMut.isPending ||
    rejectMut.isPending;

  return (
    <Card
      title={
        <Space>
          Đơn mua <b>{po.poNumber || po.id}</b> {poTag(s)}
        </Space>
      }
      extra={
        <Space wrap>
          {s === "DRAFT" && (
            <Can permission={P.INBOUND_CREATE_PO}>
              <Button
                icon={<SendOutlined />}
                loading={busy}
                onClick={() => reviewMut.mutate()}
              >
                Gửi kiểm tra
              </Button>
            </Can>
          )}
          {s === "PENDING_REVIEW" && (
            <Can permission={P.INBOUND_SUBMIT_PO}>
              <Button
                type="primary"
                icon={<SendOutlined />}
                loading={busy}
                onClick={() => approvalMut.mutate()}
              >
                Trình phê duyệt
              </Button>
            </Can>
          )}
          {s === "PENDING_APPROVAL" && user?.role === "ADMIN" && (
            <Button
              icon={<EditOutlined />}
              disabled={busy}
              onClick={() =>
                navigate("/purchase-orders/new", { state: { editPo: po } })
              }
            >
              Sửa
            </Button>
          )}
          {s === "PENDING_APPROVAL" && (
            <Can permission={P.INBOUND_APPROVE_PO}>
              <Button
                type="primary"
                icon={<CheckOutlined />}
                loading={busy}
                onClick={() => approveMut.mutate()}
              >
                Phê duyệt
              </Button>
            </Can>
          )}
          {(s === "PENDING_REVIEW" || s === "PENDING_APPROVAL") && (
            <Can permission={P.INBOUND_APPROVE_PO}>
              <Popconfirm
                title="Từ chối đơn này?"
                description={
                  <span>
                    Từ chối đơn <b>{po.poNumber || po.id}</b>.
                  </span>
                }
                okText="Từ chối"
                okButtonProps={{ danger: true }}
                cancelText="Huỷ"
                onConfirm={() => rejectMut.mutate()}
              >
                <Button danger icon={<CloseOutlined />} loading={busy}>
                  Từ chối
                </Button>
              </Popconfirm>
            </Can>
          )}
          {s === "APPROVED" && hasPermission(P.INBOUND_CREATE_GRN) && (
            <Button
              type="primary"
              icon={<InboxOutlined />}
              onClick={() =>
                navigate("/goods-receipts", { state: { poId: po.id } })
              }
            >
              Tạo phiếu nhập
            </Button>
          )}
        </Space>
      }
    >
      <Descriptions
        size="small"
        column={{ xs: 1, sm: 2, md: 3 }}
        bordered
        items={[
          {
            key: "sup",
            label: "Nhà cung cấp",
            children: supplierMap[po.supplierId]?.name || po.supplierId,
          },
          {
            key: "wh",
            label: "Kho",
            children: warehouseMap[po.warehouseId]?.name || po.warehouseId,
          },
          {
            key: "exp",
            label: "Ngày dự kiến",
            children: po.expectedDate
              ? dayjs(po.expectedDate).format("DD/MM/YYYY")
              : "—",
          },
          { key: "cb", label: "Người tạo", children: po.createdBy || "—" },
          { key: "ab", label: "Người duyệt", children: po.approvedBy || "—" },
          {
            key: "ca",
            label: "Tạo lúc",
            children: po.createdAt
              ? dayjs(po.createdAt).format("DD/MM/YYYY HH:mm")
              : "—",
          },
        ]}
      />
      <Table
        style={{ marginTop: 16 }}
        rowKey="id"
        size="small"
        pagination={false}
        dataSource={po.details || []}
        columns={columns}
        scroll={{ x: "max-content" }}
      />
    </Card>
  );
}

const fmt = (v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
const parse = (v) => v?.replace(/,/g, "");
