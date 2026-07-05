
const fileList = [
    { name: "a.txt", size: "13MB" },
    { name: "a.jar", size: "13MB" },
    { name: "b.pdf", size: "1MB" }
];
function getDataSet() {

    return {
        total: 3,
        files: fileList
    };
}

const tbody = document.querySelector('#fileTable tbody');

function renderTable() {
    tbody.innerHTML = '';
    fileList.forEach((file, index) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${file.name}</td>
            <td>${file.size}</td>
            <td>
                <button class="rename-btn" data-index="${index}">重命名</button>
                <button class="delete-btn" data-index="${index}">删除</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}


document.querySelector('#fileTable').addEventListener('click', function(e) {
    const target = e.target;
    if (target.classList.contains('delete-btn')) {
        deleteFile(parseInt(target.dataset.index));
    }
    if (target.classList.contains('rename-btn')) {
        renameFile(parseInt(target.dataset.index));
    }
});

document.getElementById('uploadBox').onclick = function() {
    document.getElementById('fileInput').click();
};


renderTable();

//获取文件列表
// GET  /api/list?path=/          ← 获取文件列表

//删除文件
// DELETE /api/file?path=/test.txt ← 删除文件

//重命名
// PUT  /api/rename?old=/test.txt&new=/new.txt  ← 重命名

//上传文件
// POST /api/upload?path=/        ← 上传文件（文件在Body中）