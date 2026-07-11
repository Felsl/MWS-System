// Chuyển object sorter của AntD Table -> tham số sort/dir cho BE.
// AntD: sorter = { field, order: 'ascend' | 'descend' | undefined }.
export function sorterToParams(sorter) {
  const s = Array.isArray(sorter) ? sorter[0] : sorter
  if (!s || !s.order || !s.field) return { sort: undefined, dir: undefined }
  const field = Array.isArray(s.field) ? s.field.join('.') : s.field
  return { sort: field, dir: s.order === 'ascend' ? 'asc' : 'desc' }
}

// sortOrder hiện tại cho 1 cột (để mũi tên sắp xếp hiển thị đúng khi controlled).
export function columnSortOrder(sorter, field) {
  const s = Array.isArray(sorter) ? sorter[0] : sorter
  return s && s.field === field ? s.order : null
}
